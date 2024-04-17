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
	    println(it)
	}
    }

    // Save it
    info['joblist'] += a
    //    waitForBuild a.externalizableId
    // test killing it
    def n = a.getFullProjectName() +"/" + a.getId()
    println(n)
    def j = Jenkins.instance.getItemByFullName(n)
    println(j)
    j.doKill()

    // If it finishes OK then we can remove it
    info['joblist'] -= a

    return a
}
