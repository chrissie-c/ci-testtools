


// Run a groovy Closure and store the results in an artifact
def call (Map info, String logfile, Closure cmd)
{
    def failed = 0
    def new_logfile = ''
    try {
	tee (logfile) {
	    cmd()
	}
    }
    catch (e) {
	failed = 1
    }
    if (failed == 1) {
	new_logfile = "FAILED_${logfile}"
	// Save it for the email
	if (!info.containsKey('failedlogs')) {
	    info['failedlogs'] = []
	}
	info['failedlogs'] += "${new_logfile}"
    } else {
	new_logfile = "SUCCESS_${logfile}"
    }
    sh "mv ${logfile} ${new_logfile}"
    archiveArtifacts artifacts: "${new_logfile}", fingerprint: false    
}
