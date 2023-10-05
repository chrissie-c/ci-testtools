
// Return the whole node array
def call()
{

    // TEST something entirely unrelated
    echo "${currentBuild.getBuildCauses()}"
    echo "${currentBuild.getBuildCauses().shortDescription}"

    def labels = [:]

    labels['rhel'] = ['rhel','rh','test1']
    labels['rhlaptop'] = ['fedora','rh', 'test2']
    labels['debian-unstable'] = ['debian', 'test2']
    
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
