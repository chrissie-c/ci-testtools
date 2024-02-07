// Generate the jobs
def call(Boolean do_zstream)
{
    // These could be parameters to the call....
    def providers = [:]
    providers['osp'] = ['maxjobs': 255, 'fatal': false, 'testlevel': 'all']
    providers['aws'] = ['maxjobs': 1, 'fatal': true, 'testlevel': 'all']
    providers['ibmvpc'] = ['maxjobs': 2, 'fatal': true, 'testlevel': 'smoke']

    def versions = ['rhel8 next-stable', 'rhel9 next-stable', 'rhel9 main']
    def branches = ['nightly','zstream']

    // Build a list of possible jobs
    def jobs = []
    for (v in versions) {
	def j = v.split(' ')
	for (b in branches) {
	    jobs += "${j[0]} ${b} ${j[1]}"
	}
    }

    // List of things to run on all providers
    def runjobs = [:]

    for (p in providers) {
	def pinfo = providers[p.key]
	def maxjobs = pinfo['maxjobs'] // max parallel
	def jobs_per_stage = Math.round((jobs.size() / maxjobs) + 0.5)
	println("jobs per stage for ${p.key}: ${jobs_per_stage} (max concurrent = ${maxjobs})")

	// Build the jobs
	def s = 0
	while (s < jobs.size()) {
	    def joblist = []
	    for (i=0; i < jobs_per_stage &&  s < jobs.size(); i++) {
		joblist += "${jobs[s]}"
		s += 1
	    }
	    println(joblist)
	    // TODO this should actually be a call runComplexStage()
	    // To be fed into 'parallel'
	    //runjobs["${p.key} ${s}"] = ['provider': p.key, 'pinfo': pinfo, 'jobs': joblist, 'zstream': do_zstream]
	    runjobs["${p.key} ${s}"] = { runComplexStage(['provider': "${p.key}", 'pinfo': pinfo, 'jobs': joblist, 'zstream': do_zstream]) }
	}
    }
    return runjobs
}

// This fn might be in a separate file (but in the same library)
def runComplexStage(Map stageinfo)
{
    def provider = stageinfo['provider']
    def pinfo = stageinfo['pinfo']
    def running = true

    println("runComplexStage: ${stageinfo}")

    for (s in stageinfo['jobs']) {
	if (true) { // running
	    def result = 0
	    stage("${s} Smoke") {
		result = sh "echo ${provider} ${s} smoke"
		result = 0
		if (result != 0 && stageinfo['fatal'] == true) {
		    running = false
		}

	    }
	    // If that succeeds and provider allows 'all' then run all
	    if (result == 0 && pinfo['testlevel'] == 'all') {
		stage("${s} All") {
		    result = sh "echo ${provider} ${s} all"
		}
		result = 0
		if (result != 0) {
		    running = false
		}
	    }
	}
    }
}


def jobs = call(true)
