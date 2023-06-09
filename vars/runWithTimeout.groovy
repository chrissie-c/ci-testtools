def call(int timeout, Closure fn, Map param)
{
    println("CC: in runWithTimeout: ${fn} ${param}");
    long startTime = System.currentTimeMillis()
    try {
	timeout(time: timeout, unit: 'MINUTES') {
	    fn(param)
	}
    } catch (err) {
	long timePassed = System.currentTimeMillis() - startTime
	if (timePassed >= timeout * 60000) {
            prinln("function ${fn} timed out")
	}
    }
}
