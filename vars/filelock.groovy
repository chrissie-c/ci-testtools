import java.io.File

// Assumes we are on node built-in
def write_lockfile(String lockfile, ArrayList contents)
{
    def outfile = new FileWriter(lockfile, false)
    for (s in contents) {
	outfile.write(s+'\n')
    }
    outfile.flush()
    outfile.close()
}

// Assumes we are on node built-in
def read_lockfile(String lockfile)
{
    return new File(lockfile) as String[]
}

// ... you get the idea
def add_us(String lockfile, String lockmode, String taskid, ArrayList current_contents)
{
    def String our_line = lockmode.substring(0,1)+taskid

    current_contents += our_line

    // Write it back
    write_lockfile(lockfile, current_contents)
}

// Called in post{always{}} (via call(,,UNLOCK,) to clear all locks for this job
def unlock_all_ours(String lockname, String lockfile, String taskid)
{
    lock(lockname) {
	node('built-in') {

	    def delete_list = []
	    def lockcontents = read_lockfile(lockfile)
	    for (s in lockcontents) {
		if (s.substring(1, s.len()) == taskid)
		    delete_list += s
	    }
	    new_list = lockcontents.minus(delete_list)

	    // Write it back
	    write_lockfile(lockfile, current_contents)
	}
    }
}

// File-based locking
//
// It works by having a file in $JENKINS_HOME/locks/F-<lockname>.locks
// that contains a list of lock owners (job URL) preceded by the lock
// mode. eg
//  Rhttps://my.jenkins.example.com/job/shave-my-hamster/86
//  Rhttps://my.jenkins.example.com/job/fix-the-universe/42
// or (for a write lock) just one line, eg
//  Whttps://my.jenkins.example.com/job/my-gay-job/69
//
// Access to this file is serialsed using normal Jenkins lock(){}
// blocks, and jobs poll for access to the file - so don't cane the system!
//
// if called with UNLOCK as the lock mode then ALL of the locks held by
// this job will be released, this is here so it can be called in post{always{}}
// to catch job failures/aborts and clean up. There is also a polymorphic
// version, 'UNLOCKALL' that can be called to check for inactive locks and tidy them up.

// NOTE: Don't disturb the types (ArrayList,String) in here, they're very carefully worked out
def call(Map info, String lockname, String mode, Closure thingtorun)
{
    def lockdir = "${JENKINS_HOME}/locks/"
    def lockfile = "${lockdir}/F-${lockname}.locks"
    def taskid = env.BUILD_URL
    def waiting = true
    def wait_time = 0

    // Called at end of pipeline in case of 'accidents'
    if (mode == 'UNLOCK') {
	unlock_all_ours(lockname, lockfile, taskid)
	return
    }

    // Validate other lock modes
    if (mode != 'WRITE' && mode != 'READ') {
	throw new Exception("Invalid lock mode ${mode}, should be either READ or WRITE")
	return
    }

    // It looks mad having this in its own block,
    // but the alternative is doing this in the wait loop,
    // which is properly insane
    node('built-in') {
	sh "mkdir -p ${lockdir}"
    }

    // Try and open the lock file
    while (waiting) {
	sleep(wait_time) // This is 0 first time round

	// Jenkins-lock the lock-file!
	lock(lockname) {
	    // Read the lock file, must happen on built-in
	    node('built-in') {
		def ArrayList lockcontents = []
		try {
		    lockcontents = read_lockfile(lockfile)
		} catch (java.io.FileNotFoundException err) {
		    // Not found = no locks. Throw all other errors back up
		}
	    }
	    if (lockcontents.size() == 0) { // No locks held, we are right in
		add_us(lockfile, mode, taskid, lockcontents)
		waiting = false
	    } else {
		if (mode == 'WRITE') { // We need to wait as something is using it
		    wait_time = 60 // a minute
		    println("${lockname} write locked - sleeping to wait for write lock")
		} else {
	            for (s in lockcontents) {
			def shortmode = s.substring(0,1)
			def jobname = s.substring(1, s.length())
			if (shortmode == 'W') {
			    wait_time = 60 // a minute
			    println("${lockname} write locked - sleeping to wait for read lock")
			} else {
			    // Must be all READ locks in the file, we are good to go
			    add_us(lockfile, mode, taskid, lockcontents)
			    waiting = false
			}
		    }
		}
	    }
	}
    }

    // Run the thing
    thingtorun()

    // Unlock it
    lock(lockname) {
	node('built-in') {
	    def ArrayList newlockcontents = []

	    // If we have a READ lock, we need to re-read the file in case other readers have appeared
	    if (mode == 'READ') {
		def ArrayList lockcontents = read_lockfile(lockfile)
		def our_line = [ mode.substring(0,1)+taskid ]

		// Remove us from the list
		newlockcontents = lockcontents.minus(our_line)
	    }

	    // Write it back
	    write_lockfile(lockfile, newlockcontents)
	}
    }
}

// Polymorph to tidy up inactive jobs for a lock,
// lockmode must be 'UNLOCKALL' - this is just documentation reasons.
// Of course, if the system is quiescent you can just delete the file!
def call(Map info, String lockname, String lockmode)
{
    // Punish peple who didn't read that comment
    if (lockmode != 'UNLOCKALL') {
	println('I TOLD YOU that lockmode must be UNLOCKALL')
	return
    }

    def lockdir = "${JENKINS_HOME}/locks/"
    def lockfile = "${lockdir}/F-${lockname}.locks"

    // Hold the lock for the duration of this
    lock(lockname) {
	node('built-in') {
	    def ArrayList lockcontents = read_lockfile(lockfile)
	    def deletelist = []

	    // Look through the Jenkins job list
	    Jenkins.instance.getAllItems(Job).each {
		def jobBuilds = it.getBuilds()
		for (i = 0; i < jobBuilds.size(); i++) {
		    jobBuilds[i].each { build ->
			println("Seen job ${build}")
			if (!build.isInProgress()) {
			    def curjoburl = "${env.JENKINS_URL}${build.getUrl()}"

			    // Look for it in the lockfile
			    for (s in lockcontents) {
				def joburl = s.substring(1, s.length())
				if (curjoburl == joburl) {
				    found = true; // Job is not running, remove it
				    deletelist += s
				}
			    }
			}
		    }
		}
	    }
	    def ArrayList newlocklist = lockcontents.minus(deletelist)
	    println("Global UNLOCKALL for ${lockname}, deletelist is ${deletelist} new locklist is ${newlocklist}")
	    write_lockfile(lockfile, newlocklist)
	}
    }
}

// Example
def unlock_everything(Map info)
{
    def JENKINS_HOME='/home/christine/tmp'
    def String lockdir = "${JENKINS_HOME}/locks/"

    def lockdir_it = new File(lockdir)
    lockdir_it.eachFile() {
	def basename = it.toString().substring(lockdir.length())
	if (basename.substring(0, 2) == 'F-' &&
	    basename.substring(basename.length()-6) == '.locks') {
	    def lockname = basename.substring(2, basename.length()-6)
	    //call(info, lockname, 'UNLOCKALL')
	    println("unlocking ${lockname}")
	}
    }
}
