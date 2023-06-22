
pipeline {
    // I prefer to have a dedicated node to execute admin tasks
    agent {
        label "admin-agent"
    }
        
    options {
        timestamps()
    }
        
    stages {
        stage('agents-tasks') {
            
            steps {
                script {
		    collectBuildEnv = collectenv('rh')
      
                    parallel collectBuildEnv
                }
            }
        }
    }
}
