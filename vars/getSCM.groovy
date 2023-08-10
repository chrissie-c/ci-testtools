def call()
{
    if (env.NODE_NAME == 'built-in') {
	dir ('project') {
	    checkout scm
	}
	sh 'tar -czf gitfiles.tgz project'
	archiveArtifacts artifacts: "gitfiles.tgz", fingerprint: false
    } else {
	sh "wget ${env.BUILD_URL}artifact/gitfiles.tgz"
	sh "tar -xzf gitfiles.tgz"
	// sh "rm gitfiles.tgz"
    }

	
	

}
