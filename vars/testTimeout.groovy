def call(Integer time, String cmd)
{
    def retval = 0
    println("In testTimeout")

    
    try {
	timeout(time: time, unit: 'SECONDS' ) {
	    sh "${cmd}"
	}
    } catch (hudson.AbortException ae) { // Script Error
	println("Script error: ${ae}")
	retval = 1
    } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException err) {
	def cause = err.getCauses()[0]
	if (cause.startsWith('org.jenkinsci.plugins.workflow.steps.TimeoutStepExecution$ExceededTimeout')) {
	    println('Timeout exceeded')
	    retval = 2
	}
	else if (cause.startsWith('jenkins.model.CauseOfInterruption$UserInterruption')) {
	    println('User abort')
	    retval = 3
	}
    }
    return retval
}
