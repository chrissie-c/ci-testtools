// Just to see if this works
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;


def call(String lockname, String mode, Closure thingtorun)
{
    def lockmode = 0; // LOCK_SH
    if (mode == "READ") {
	lockmode = 1; // LOCK_SH
    }
    if (mode == "WRITE") {
	lockmode = 2; // LOCK_EX
    }
    if (lockmode == 0) {
	return -1
    }
    
    def fd = jnaflock.CLibrary.INSTANCE.creat("/tmp/${lockname}", 0666)
    println("FD for test.lock is " + fd)
    jnaflock.CLibrary.INSTANCE.flock(fd, 1) // LOCK_SH
    thingtorun()
    jnaflock.CLibrary.INSTANCE.close(fd)
    println("Closed")
}
