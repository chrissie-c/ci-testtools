import Jenkins.instance.*

@NonCPS
def call()
{
    allnodes=[]
    a= Jenkins.instance.getNodes()
    for (i in a) {
	allnodes += i.getNodeName()
    }
    return allnodes
}

