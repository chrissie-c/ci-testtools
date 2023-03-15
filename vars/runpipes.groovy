def call(Map config = [:]) {
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
			    runstuff(project:config.project, branch:config.branch, makeopts:"all test")
			}
                    }
		    stage('Build RPM') {
                        steps {
			    runstuff(project:config.project, branch:config.branch, makeopts:"rpm")
//  		            archiveArtifacts artifacts: "${config.project}*.rpm, x86_64/*rpm", fingerprint: false
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