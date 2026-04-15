
// Generate a run-list
def call(Map info, Closure fn)
{
    def collectBuildEnv = [:]

    for (def j in jobs) {
	def job = j // needs to be local to the loop
	collectBuildEnv[job] = {
	    fn(job, info)
	}
    }

    return collectBuildEnv
}
