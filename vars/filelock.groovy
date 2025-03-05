import java.io.File;


// Assumes we are on node built-in
def write_file(String lockfile, String[] contents)
{
    def outfile = new FileWriter(lockfile, false)
    for (s in contents) {
	outfile.write(s+'\n')
    }
    outfile.flush()
    outfile.close()
}

def add_us(String lockfile, String lockmode, String taskid, String[] current_contents)
{
    def our_line = lockmode.substring(0,1)+taskid

    current_contents += our_line

    println("CC: add_us: lines = ${lockmode} ${current_contents}")

    // Write it back
    node('built-in') {
	write_file(lockfile, current_contents)
    }
}


// Called in post{always{}} to clear all locks for this job
def unlock_all(String lockname, String lockfile, String taskid)
{
    lock(lockname) {
	node('built-in') {

	    def delete_list = []
	    def lockcontents = new File(lockfile as String) as String[]
	    for (s in lockcontents) {
		if (s.substring(1, s.len()) == taskid)
		    delete_list += s
	    }
	    new_list = lockcontents.minus(delete_list)

	    // Write it back
	    write_file(lockfile, current_contents)
	}
    }
}

// THIS is the new File-based locking
def call(Map info, String lockname, String mode, Closure thingtorun)
{
    def lockdir = "${JENKINS_HOME}/locks/"
    def lockfile = "${lockdir}/F-${lockname}.locks"
    def taskid = env.BUILD_URL
    def waiting = true
    def wait_time = 0

    // Jenkins-lock the lock-file!
    while (waiting) {
	sleep(wait_time)
	lock(lockname) {
	    // Read the existing file - DO THIS ON built-in
	    def String[] lockcontents = []	    
	    node('built-in') {
		sh "mkdir -p ${lockdir}"		
		try {
		    lockcontents = new File(lockfile as String) as String[]
		} catch (err) {
		    // java.io.FileNotFoundException:
		    // Not found. assume empty
		}
	    }
	    if (lockcontents.size() == 0) {
		println("CC: No locks in file - good to go")
		add_us(lockfile, mode, taskid, lockcontents)
		waiting = false
	    } else {
		if (mode == 'WRITE') { // we need to wait as something is using it
		    wait_time = 1
		    println("${lockname} write locked - sleeping to wait for lock");
		} else {
	            for (s in lockcontents) {
			println("CC: Seeing if we can allow read lock")
			def shortmode = s.substring(0,1)
			def jobname = s.substring(1, s.length())
			if (shortmode == 'W') {
			    wait_time = 1
			    println("${lockname} write locked - sleeping to wait for read lock");
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
    }

    // Run the thing
    thingtorun()

    // Unlock it
    // Re-Read the existing file to get ay READer updates
	lock(lockname) {
	    def lockcontents = new File(lockfile) as String[]
	def our_line = [ mode.substring(0,1)+taskid ]

	// Remove us from the list
        def newlockcontents = lockcontents.minus(our_line)

	// Write it back
	node('built-in') {
	    write_file(lockfile, newlockcontents);
    }
}

