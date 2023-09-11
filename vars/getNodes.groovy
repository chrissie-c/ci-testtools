
// Return the whole node array
def call()
{
    def labels = [:]

    nodes['rhel'] = ['rhel','rh','test1']
    nodes['rhlaptop'] = ['fedora','rh', 'test2']
    
    return labels
}

// Return the nodes that have a specific label
def call(String target)
{
    def labels = call()

    def nodelist = []
    for (i in labels) {
	if (labels[i.key].contains(target)) {
	    nodelist += i.key
	}
    }
    return nodelist
}
