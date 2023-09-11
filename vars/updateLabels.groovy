import Jenkins.instance.*

def call()
{
    println("CC: updateLabels")
    def nodes = getNodes()
    def newlabels_arr = nodes[env.NODE_NAME]

    println("CC: Update Labels on ${env.NODE_NAME} - array="+newlabels_arr)

    // Don't update built-in or other nodes with no labels in getNodes()
    if (newlabels_arr == null || newlabels_arr.size() == 0) {
	return
    }

    // Convert the array to a long string
    def newlabels_str = ''
    for (i in newlabels_arr) {
	newlabels_str += " ${i}"
    }

    // Get the existing labels, so we don't lose "down" if it's present
    def node_handle = Jenkins.instance.getNode(env.NODE_NAME)
    def curlabels = node_handle.getLabelString().split()
    if (curlabels.contains('down')) {
	newlabels_str += " down"
    }
    println("CC: old labels: ${curlabels}")
    println("CC: new labels: ${newlabels_str}")

    // Update Jenkins
    node_handle.setLabelString(newlabels_str)
    node_handle.save() // Write it to disk
}
