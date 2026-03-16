


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

    } else {
	new_logfile = "SUCCESS_${logfile}"
    }
    sh "mv ${logfile} ${new_logfile}"
}
