 def call(Integer time, String cmd)
{
    def retval = 0
    
    try {
	timeout(time: time, unit: 'SECONDS') {
	    sh "${cmd}"
	}
    } catch (hudson.AbortException ae) { // Script Error
	// This is actually an abort
	// https://gist.github.com/stephansnyt/3ad161eaa6185849872c3c9fce43ca81?permalink_comment_id=2198976
	if (ae.getMessage().contains('script returned exit code 143')) {
	    println('"code 143" abort')
	    retval = 3
	} else {
	    println('Script exit')
	    retval = 1
	}
    } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException err) {
	// If no 'cause' is given then we don't know what happened
	// so just rethrow it.
	if (err.getCauses() == 0) {
	    throw (err)
	}
	def String cause = err.getCauses()[0]
	if (cause.startsWith('org.jenkinsci.plugins.workflow.steps.TimeoutStepExecution$ExceededTimeout')) {
	    println('Timeout exceeded')
	    retval = 2
	}
	else if (cause.startsWith('jenkins.model.CauseOfInterruption$UserInterruption')) {
	    println('User abort')
	    retval = 3
	} else {
	    // Not for us - rethrow it
	    throw(err)
	}
    }
    return retval
}
