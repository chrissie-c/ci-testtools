import jenkins.model.*

@NonCPS
def getNodes(String label) {
    jenkins.model.Jenkins.instance.nodes.collect { thisAgent ->
        if (thisAgent.labelString.contains("${label}")) {
        // this works too
        // if (thisAagent.labelString == "${label}") {
            return thisAgent.name
        }
    }
}

def dumpBuildEnv(String agentName) {
    node("${agentName}") {
        stage("Env in ${agentName}") {
            echo "running on agent, ${agentName}"
            sh 'uname -a'
	    sh 'printenv'
        }
    }
}

def call(String label) {
    def nodeList = getNodes(label)
    collectBuildEnv = [:]

    for(i=0; i<nodeList.size(); i++) {
        def agentName = nodeList[i]

        // skip the null entries in the nodeList
        if (agentName != null) {
            println "Prearing task for " + agentName
            collectBuildEnv["node_" + agentName] = {
                dumpBuildEnv(agentName)
            }
        }
    }
    return collectBuildEnv
}
