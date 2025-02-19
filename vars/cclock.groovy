// Just to see if this works
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;


import com.redhat.clusterci.jnaflock

def call(String name)
{
    def a = new jnaflock('tmp/test.lock')
    a.lock('EX')
    println(a)
    sleep(5000)
    a.unlock()
}
