import Jenkins.instance.*

def call()
{    
    def nodes = getNodes()
    def newlabels_arr = nodes[env.NODE_NAME]

    // Don't update built-in or other nodes with no labels in getNodes()
    if (newlabels_arr.size() == 0) {
	return
    }
    
    // Convert the array to a long string
    def newlabels_str = ''
    for (i in newlabels_arr) {
	newlabels_str += " ${i}"
    }
    
    // Get the existing labels, so we don't lose "down" if it's present
    def node_handle = Jenkins.instance.getNode("rhel")
    def curlabels = node_handle.getLabelString().split()
    if (curlabels.contains('down')) {
	newlabels += " down"
    }
    println("CC: old labels: ${curlabels}")
    println("CC: new labels: ${newlabels}")    

    // Update Jenkins
    node_handle.setLabelString(newlabels)
    node_handle.save() // Write it to disk
}
