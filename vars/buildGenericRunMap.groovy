
def nodes=['debian', 'rhel', 'fedora']

def runit(Map info, String agentName, Closure fn)
{
    node(agentName) {
	fn()
    }
}

// Generate a run-list
def call(Map info, ArrayList nodes, Closure fn)
{

    def collectBuildEnv = [:]

    for (def j in jobs) {
	def job = j // needs to be local to the loop
	collectBuildEnv[job] = ''
	}
    }

    return collectBuildEnv
}
