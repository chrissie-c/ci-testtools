import Jenkins.instance.*

@NonCPS
def call()
{
    def allnodes=[]
    a = Jenkins.instance.getNodes()
    for (i in a) {
	def node = i.getNodeName()
	allnodes += node
    }
    return allnodes
}

