import jenkins.model.*

// Returns all nodes that have the label
@NonCPS
def getNodes(String label) {
    jenkins.model.Jenkins.instance.nodes.collect { thisAgent ->
        if (thisAgent.labelString.contains("${label}")) {
        // this works too
        // if (thisAagent.labelString == "${label}") {
            return thisAgent.name
        }
	else {
	    println("CC: got node ${thisAgent} with ${thisAgent.labelString}");
	}
    }
}

@NonCPS
def test1()
{
    def a=[:]
    return a
}

def test2()
{
    def a=[:]
    return a
}

// Builds up a map with all the nodes and the functions (steps) to call.
// Pass the returned map to 'parallel'
def call(String label, Closure stepfunc) {
    def nodeList = getNodes(label)

    def t1 = test1()
    def t2 = test2()
    println('CC: type of t1 is'+t1.getClass())
    println('CC: type of t2 is'+t2.getClass())
    
    collectBuildEnv = [:]

    for(i=0; i<nodeList.size(); i++) {
        def agentName = nodeList[i]

        // skip the null entries in the nodeList
        if (agentName != null) {
            collectBuildEnv["node_" + agentName] = {
                stepfunc(agentName)
            }
        }
    }
    return collectBuildEnv
}
