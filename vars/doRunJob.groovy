// doRunJob.groovy

def call(String jobname, ArrayList params, Map info)
{
    def a = build job: jobname,
	parameters: params,
	propagate: false
//	wait: false

    println(a)
    println(a.getNumber())
    info['joblist'] += a

    a.waitForBuild()

    info['joblist'] -= a

    return a
}
