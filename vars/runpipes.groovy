def call(Map pipelineParams = [:]) {

    pipeline {
	agent none
	stages {
            stage('Build and Test') {
		environment {
	    	    PROJECT = "${pipelineParams.project}"
		    BRANCH = "${pipelineParams.branch}"
		}
		
		matrix {
                    agent {
			label "${PLATFORM}"
                    }
                    axes {
			axis {
                            name 'PLATFORM'
                            values 'fedora', 'debian', 'rhel'
			}
                    }
                    stages {
			stage('Prep') {
		            steps {
				script {
				    def c = getContributors('ClusterLabs','libqb')
				}
				echo "${c}"

				echo "${pipelineParams}"				
				sh "sh autogen.sh"
				sh "./configure"
			    }
			}
			stage('Build') {
                            steps {
				runstuff(project:"$PROJECT", branch:"$BRANCH", makeopts:"all")
			    }
			}
			stage('Test') {
                            steps {
				runstuff(project:"$PROJECT", branch:"$BRANCH", makeopts:"check")
			    }
			}
			stage('Build tarball') {
                            steps {
				runstuff(project:"$PROJECT", branch:"$BRANCH", makeopts:"dist")
  				archiveArtifacts artifacts: "$PROJECT*.tar.gz, $PROJECT*.tar.xz", fingerprint: false
			    }
			}
		    }
		    post {
			always {
			    sh "uname -a"
			}
		    }
		}
	    }
	}
    }
}
