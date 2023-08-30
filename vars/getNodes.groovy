@NonCPS
def call(String label)
{
    jenkins.model.Jenkins.instance.nodes.collect { thisAgent ->
	labelarray = thisAgent.labelString.split(' ')
	println("CC: test: "+thisAgent.name)
        if (labelarray.contains(label)) {
            return thisAgent.name
        }
    }
}

