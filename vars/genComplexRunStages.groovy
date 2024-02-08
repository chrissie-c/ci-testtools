// Generate the jobs
def call(String tests, Boolean dryrun)
{
    // These could be parameters to the call....
    def providers = [:]
    providers['osp'] = ['maxjobs': 3, 'testlevel': 'all']
    providers['aws'] = ['maxjobs': 1, 'testlevel': 'smoke']
    providers['ibmvpc'] = ['maxjobs': 255, 'testlevel': 'all']

    // Think about this? rexexp ???
//    def testexcludes=[:]
//    testexcludes['ibmvpc'] = 'watchdog'

    // OS/upstream versions as pairs
    def versions = [['8', 'next-stable'], ['9', 'next-stable'], ['9',' main']]
    def zstream = ['no','yes']

    // Build a list of possible jobs
    def jobs = []
    for (v in versions) {
	for (b in zstream) {
	    jobs += ['rhelver': v[0], 'zstream': b, 'upstream': v[1]]
	}
    }
    println("JOBS: "+jobs)

    // List of things to run on all providers
    def runjobs = [:]

    // Build the jobs list
    for (p in providers) {
	def prov = p.key
	def pinfo = providers[prov]
	def maxjobs = pinfo['maxjobs'] // max parallel
	def jobs_per_stage = Math.round((jobs.size() / maxjobs) + 0.5)

	// Build the jobs
	def s = 0
	while (s < jobs.size()) {
	    def joblist = []
	    for (i=0; i < jobs_per_stage &&  s < jobs.size(); i++) {
		joblist += "${jobs[s]}"
		s += 1
	    }
	    runjobs["${prov} ${s}"] = { runTestStages(['provider': prov, 'pinfo': pinfo, 'jobs': joblist, 'tests': tests,
						       'dryrun': dryrun]) }
	}
    }
    // Feed this into 'parallel'
    return runjobs
}

// This fn might be in a separate file (but in the same library)
def runTestStages(Map stageinfo)
{
    def provider = stageinfo['provider']
    def pinfo = stageinfo['pinfo']
    def running = true

    println("rcs: provider: ${provider}")
    println("rcs: pinfo: ${pinfo}");
    println("runComplexStage: ${stageinfo}")

    for (s in stageinfo['jobs']) {
	if (true) { // running
	    def result = 0
	    // remember - s is also a map :)
	    stage("${s} Smoke") {
		result = sh "echo ${provider} ${s} smoke"
		result = 0 // TEST
		if (result != 0) {
		    running = false
		}

	    }
	    // If that succeeds and provider allows 'all' then run all
	    if (result == 0 && pinfo['testlevel'] == 'all') {
		stage("${s} All") {
		    result = sh "echo ${provider} ${s} all"
		}
		result = 0 // TEST
		if (result != 0) {
		    running = false
		}
	    }
	}
    }
}

// TEST in standalone groovy
def jobs = call('all', true)
