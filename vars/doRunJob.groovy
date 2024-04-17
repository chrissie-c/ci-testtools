// doRunJob.groovy

def call(String jobname, ArrayList params, Map info)
{
    def a = build job: jobname,
		  parameters: params,
		  propagate: false,
		  waitForStart: true,
		  wait: false

    // Save it
    jobId = "${a.getFullProjectName()} #${a.getId()}") {
    info['joblist'] += jobId

    Jenkins.instance.getItemByFullName(a.getFullProjectName()).each {
	for (b in it.getBuilds()) {
	    if (b.isInProgress()) {
		def String name = b
		if (info['joblist'].contains(name)) {
		    println("Stopping job: "+b)
		    if (name == "${a.getFullProjectName()} #${a.getId()}") {
			b.doStop()
		    }
		}
	    }
	}
    }

    waitForBuild a.externalizableId

    // If it finishes OK then we can remove it
    info['joblist'] -= jobId

    return a
}
