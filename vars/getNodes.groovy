@NonCPS
def call(String label)
{
    jenkins.model.Jenkins.instance.nodes.collect { thisAgent ->
       labelarray = thisAgent.labelString.split(' ')
        if (labelarray.contains(label)) {
	    println("got ${thisAgent.name}"
            return thisAgent.name
        }
    }
}

