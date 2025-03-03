// Provide READ/WRITE locking in Jenkins, using flock over jna
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

class jnaflock {
    // com.sun.jna.Library need to be named explicity for some reason.
    interface CLibrary extends com.sun.jna.Library {
	CLibrary INSTANCE = (CLibrary)Native.load("c", CLibrary.class);

	int creat(String file, int mode);
	int flock(int fd, int mode);
	int close(int fd);
    }
}

// Called in post {always{}} to make sure the file is closed and unlocked, called from main fn so
// is already running on 'built-in'.
def do_unlock(Map info)
{
    println("do_unlock: info map: ${info}")

    if (info.containsKey('lockfd') && info['lockfd'] >= 0) {
	if (jnaflock.CLibrary.INSTANCE.flock(info['lockfd'], 8) == -1) { // 8 = LOCK_UNLOCK
	    println("do_unlock failed on fd ${info['lockfd']}")
	    return
	}
	jnaflock.CLibrary.INSTANCE.close(info['lockfd'])
	println("Lock on fd ${info['lockfd']} released")
    }
}

def call(Map info, String lockname, String mode, Closure thingtorun)
{
    if (info['lockfd'] != -1) {
	throw(new Exception("Request for lock ${lockname}, while lock on fd ${info['lockfd']} already held (only 1 lock allowed at a time)"))
	return -1
    }

    def lockdir = "${JENKINS_HOME}/locks"

    sh "mkdir -p ${lockdir}"

    def lockmode = 0
    if (mode == 'READ') {
	lockmode = 1 // LOCK_SH
    }
    if (mode == 'WRITE') {
	lockmode = 2 // LOCK_EX
    }
    if (mode == 'UNLOCK') {
	do_unlock(info)
	return -1 // Doesn't actually 'return' - more like a 'break' ???!!
    }
    if (lockmode == 0) {
//	throw(new Exception("jnaflock: Unknown lock mode ${mode}"))
	return -1
    }
    
    // This MUST run on the Jenkins host
    node('built-in') {
	info['lockfd'] = jnaflock.CLibrary.INSTANCE.creat("${lockdir}/${lockname}.lock", 0666)
	if (info['lockfd'] == -1) {
	    throw(new Exception("Failed to 'creat' file for lock ${lockdir}/${lockname}"))
	    return -1
	}
	println("FD for lock ${lockname} is " + info['lockfd'])
	jnaflock.CLibrary.INSTANCE.flock(info['lockfd'], lockmode)
    }

    println("After first node() block")
    
    thingtorun()
    
    node('built-in') {
	println("in second node() block")
	jnaflock.CLibrary.INSTANCE.close(info['lockfd'])
	println("Lock ${lockname} released")
	info['lockfd'] = -1
    }
    return 0
}
