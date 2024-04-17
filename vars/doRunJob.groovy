// doRunJob.groovy

def call(String jobname, Map params, Map info)
{
    def a = build job: jobname,
	parameters: params,
	propogate: false,
	wait: false

    info['joblist'] += a

    a.waitForBuild()

    info['joblist'] -= a

    return a
}
