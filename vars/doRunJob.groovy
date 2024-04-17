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
    println(a.getAbsoluteUrl())
    println(a.getFullProjectName())
    println(a.getFullDisplayName())
    println(a.externalizableId)

    info['joblist'] += a

    waitForBuild a.externalizableId

    info['joblist'] -= a

    return a
}
