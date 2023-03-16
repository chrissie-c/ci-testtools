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
                        values 'fedora', 'debian'
                    }
                }
                stages {
                    stage('Build & Test') {
                        steps {
			    runstuff(project:"$PROJECT", branch:"$BRANCH", makeopts:"all check")
			}
                    }
		    stage('Build RPM') {
                        steps {
			    runstuff(project:"$PROJECT", branch:"$BRANCH", makeopts:"rpm")
  		            archiveArtifacts artifacts: "$PROJECT*.rpm, x86_64/*rpm", fingerprint: false
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