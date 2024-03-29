// Generate the jobs
def call(String tests, Boolean dryrun)
{
    // Cloud providers and their limits
    def providers = [:]
    providers['osp'] = ['maxjobs': 3, 'testlevel': 'all']
//    providers['aws'] = ['maxjobs': 1, 'testlevel': 'smoke']
//    providers['ibmvpc'] = ['maxjobs': 255, 'testlevel': 'all']

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

    // Build the jobs, and decide what can be run in parallel and what
    // needs to be serialised
    def runjobs = [:]
    for (p in providers) {
	def prov = p.key
	def pinfo = providers[prov]

	// Work out how many stages we need to run in serial to keep under the
	// provider's instance limit
	def jobs_per_stage = Math.max(Math.round((jobs.size() / pinfo['maxjobs'])), 1)

	// And divide them up...
	def s = 0
	while (s < jobs.size()) {
	    def start_s = s
	    def joblist = []
	    for (i=0; i < jobs_per_stage &&  s < jobs.size(); i++) {
		joblist += jobs[s]
		s += 1
	    }
	    runjobs["${prov} ${start_s+1}-${s}"] = { runTestStages(['provider': prov, 'pinfo': pinfo, 'jobs': joblist,
								    'tests': tests, 'dryrun': dryrun]) }
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

    println("runComplexStage: ${stageinfo}")

    for (s in stageinfo['jobs']) {
	if (true) { // running
	    def result = 0
	    // remember - s is also a map of job params
	    stage("rhel${s['rhelver']} ${s['zstream']} ${s['upstream']} Smoke") {
		def thisjob1 = build job: 'global/ha-functional-testing',
		    parameters: [[$class: 'LabelParameterValue', name: 'provider', label: provider],
				 string(name: 'dryrun', value : "${stageinfo['dryrun']}"),
				 string(name: 'rhelver', value: "${s['rhelver']}"),
				 string(name: 'zstream', value: "${s['zstream']}"),
				 string(name: 'upstream', value: "${s['upstream']}"),
				 string(name: 'tests', value: 'smoke')]
		// CC: Not sure what to do with ${stageinfo['tests']}

		if (thisjob.result != 'SUCCESS') {
		    running = false
		}
	    }
	    // If that succeeds and provider allows 'all' then run other tests
	    if (result == 0 && pinfo['testlevel'] == 'all') {
		stage("rhel${s['rhelver']} ${s['zstream']} ${s['upstream']} All") {
		def thisjob2 = build job: 'global/ha-functional-testing',
		    parameters: [[$class: 'LabelParameterValue', name: 'provider', label: provider],
				 string(name: 'dryrun', value : "${stageinfo['dryrun']}"),
				 string(name: 'rhelver', value: "${s['rhelver']}"),
				 string(name: 'zstream', value: "${s['zstream']}"),
				 string(name: 'upstream', value: "${s['upstream']}"),
				 string(name: 'tests', value: "${stageinfo['tests']}")]
		    // CC: Not sure what to do with ${stageinfo['tests']}
		}
	    }
	}
    }
}

// TEST in standalone groovy
//              tests, dryrun
def jobs = call('all', true)
println(jobs)
