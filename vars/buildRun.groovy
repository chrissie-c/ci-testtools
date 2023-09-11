def runStuff(String nodename, Map info)
{
    println("runstuff: ${i}")
    // node("${nodename}") {
    // 	stage("do it on ${nodename}") {
    // 	    println("${env.NODE_NAME}")
    // 	}
    // }
}

def call(Map info)
{
    def runmap = [:]
    def allnodes = getAllNodes()
    for (i=0; i< allnodes.size(); i++) {
	runmap[allnodes[i]] = {
	    runstuff(allnodes[i], info)
	}
    }
    return runmap
}
