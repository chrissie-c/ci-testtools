// doRunJob.groovy
def call(String jobname, ArrayList params, Map info)
{
    def a = build job: jobname,
		  parameters: params,
		  propagate: false,
		  waitForStart: true,
		  wait: false

    // Save it in case we get aborted before it all completes
    String jobId = "${a.getFullProjectName()} #${a.getId()}"
    info['joblist'] += jobId

    // Wait for it to finish
    waitForBuild a.externalizableId

    // If it finishes OK then we can remove it from the list
    info['joblist'] -= jobId

    return a
}
