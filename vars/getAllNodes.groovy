import Jenkins.instance.*

@NonCPS
def call()
{
    def allnodes=[]
    
    a = Jenkins.instance.getNodes()
    for (i=0; i<a.size(); i++) {
	def node = a[i].getNodeName()
	allnodes += node
    }
    return allnodes
}

