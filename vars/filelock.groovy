import java.io.File;

def add_us(String lockfile, String lockmode, String taskid, String[] current_contents)
{
    def our_line = lockmode.substring(0,1)+taskid

    current_contents += our_line

    println("CC: add_us: lines = ${lockmode} ${current_contents}")

    // Write it back
    node('built-in') {
	def outfile = new FileWriter(lockfile, false)
	for (s in current_contents) {
	    outfile.write(s+'\n')
	}
	outfile.flush()
	outfile.close()
    }
}


// THIS is the new File-based locking
def call(Map info, String lockname, String mode, Closure thingtorun)
{
    def lockdir = "${JENKINS_HOME}/locks/"
    def lockfile = "F-${lockname}.locks"
    def taskid = env.BUILD_URL
    def waiting = true

    // Jenkins-lock the lock-file!
    while (waiting) {
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
		    sleep(1000)
		    println("${lockname} write locked - sleeping to wait for lock");
		    continue
		}

	        for (s in lockcontents) {
		    println("CC: Seeing if we can allow read lock")
		    def shortmode = s.substring(0,1)
		    def jobname = s.substring(1, s.length())
		    if (shortmode == 'W') {
			sleep(1000)
			println("${lockname} write locked - sleeping to wait for read lock");
			continue
		    }
		    // Must be all READ locks in the file, we are good to go
		    add_us(lockfile, mode, taskid, lockcontents)
		    waiting = false
		}
	    }
	}
    }
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
	    outfile = new FileWriter(lockfile, false)
	    for (s in newlockcontents) {
		outfile.write(s)
	    }
	    outfile.flush()
	    outfile.close()
	}
    }
}
