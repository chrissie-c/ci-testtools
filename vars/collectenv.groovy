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


// Builds up a map with all the nodes and the functions (steps) to call.
// Pass the returned map to 'parallel'
def call(String label, Closure stepfunc) {
    def nodeList = getNodes(label)
    
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
