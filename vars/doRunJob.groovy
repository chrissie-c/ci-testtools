// doRunJob.groovy

def call(String jobname, ArrayList params, Map info)
{
    def a = build job: jobname,
		  parameters: params,
		  propagate: false,
		  waitForStart: true,
		  wait: false

    println(a)

    info['joblist'] += a

    waitForBuild a.getDisplayName()

    info['joblist'] -= a

    return a
}
