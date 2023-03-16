def call(Map params = [:]) {

  echo "runpipes: params = ${params}"
  def pipelineParams = params
  echo "runpipes: pipelineParams = ${pipelineParams}"
  
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
		            sh "sh autogen.sh"
			    sh "./configure"
			}
		    }
                    stage('Build') {
                        steps {
			    runstuff(project:"$PROJECT", branch:"$BRANCH", makeopts:"check")
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