@NonCPS
def call(String label)
{
    def nodelist = []
    for thisAgent in jenkins.model.Jenkins.instance.nodes {
	labelarray = thisAgent.labelString.split(' ')
        if (labelarray.contains(label)) {
            nodelist += thisAgent.name
        }
    }
    return nodelist
}
