// doRunJob.groovy

def call(String jobname, ArrayList params, Map info)
{
    def a = build job: jobname,
		  parameters: params,
		  propagate: false,
		  waitForStart: true,
		  wait: false


    println(a)
    println(a.getId())
    println(a.getFullProjectName())
    println(a.externalizableId)

    Jenkins.instance.getItemByFullName(a.getFullProjectName()).each {
	for (b in it.getBuilds()) {
	    if (b.isInProgress()) {
		def String name = b
		println("job: "+b)
		if (name == "${a.getFullProjectName()} #${a.getId()}") {
		    b.doStop()
		}
	    }
	}
    }

    // Save it
    info['joblist'] += a
    //    waitForBuild a.externalizableId

    // If it finishes OK then we can remove it
    info['joblist'] -= a

    return a
}
