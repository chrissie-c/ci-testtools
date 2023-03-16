def call(Map params = [:]) {

  echo "runpipes: params = ${params}"
  def pipelineParams = params
  echo "runpipes: pipelineParams = ${pipelineParams}"
  
  pipeline {
    agent none
      stages {
        stage('Build and Test') {
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
			    runstuff(project:"${pipelineParams.project}", branch:"${pipelineParams.branch}", makeopts:"all test")
			}
                    }
		    stage('Build RPM') {
                        steps {
			    runstuff(project:"${pipelineParams.project}", branch:"${pipelineParams.branch}", makeopts:"rpm")
  		            archiveArtifacts artifacts: "${pipelineParams.project}*.rpm, x86_64/*rpm", fingerprint: false
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