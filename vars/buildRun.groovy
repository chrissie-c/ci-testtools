
def runStuff(String nodename, Map info)
{
    prinln("runstuff: ${i}")
    node("${nodename}") {
	stage("do it on ${nodename}") {
	    println("${env.NODE_NAME}")
	}
    }
}

def call(Map info)
{
    def runmap = [:]
    def allnodes = getAllNodes()
    for (i in allnodes) {
	runmap[i] = {
	    echo $i
	}
    }
    return runmap
}
