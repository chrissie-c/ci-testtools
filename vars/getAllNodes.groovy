import Jenkins.instance.*

@NonCPS
def call()
{
    def nodelist =[]
    for (thisAgent in jenkins.model.Jenkins.instance.nodes) {
        labelarray = thisAgent.labelString.split(' ')
        nodelist += thisAgent.name
    }
    return nodelist
}

