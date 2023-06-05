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
			wildcard()
			stage('Prep') {
		            steps {
				script {
				    timeStamp = Calendar.getInstance().getTime().format('YYYYMMdd-hhmmss',TimeZone.getTimeZone('CST'))				    
				}
				sh "echo CC: $timeStamp"
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
				runstuff(project:"$PROJECT", branch:"$BRANCH", makeopts:"")
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
