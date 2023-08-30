@NonCPS
def call(String label)
{
    jenkins.model.Jenkins.instance.nodes.collect { thisAgent ->
       labelarray = thisAgent.labelString.split(' ')
        if (thisAgent.name != null && labelarray.contains(label)) {
            return thisAgent.name
        }
    }
}

