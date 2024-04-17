// doRunJob.groovy

def call(String jobname, ArrayList params, Map info)
{
    def a = build job: jobname,
		  parameters: params,
		  propagate: false,
		  waitForStart: true,
		  wait: false

    // Save it
    info['joblist'] += a
    //    waitForBuild a.externalizableId
    a.doKill()

    // If it finishes OK then we can remove it
    info['joblist'] -= a

    return a
}
