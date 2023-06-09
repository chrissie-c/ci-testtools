pipeline {
    parameters {
        string(name: 'NODES', defaultValue: 'fedora37', description: 'Set of nodes to run on')
    }
    agent {
	label "$params.NODES"
    }
    stages {
	stage('Build and test') {
	    stages {
                stage('Prep') {
                    steps {
                        sh 'sh autogen.sh'
                        sh 'PKG_CONFIG_PATH=/srv/knet/origin/main/lib/pkgconfig/ ./configure'
                    }
                }
                stage('Build') {
                    steps {
			sh 'make all'
                    }
                }
                stage('Test') {
                    steps {
			sh 'make check'
                    }
                }
                stage('Dist Check') {
                    steps {
			sh 'make distcheck DISTCHECK_CONFIGURE_FLAGS="PKG_CONFIG_PATH=/srv/knet/origin/main/lib/pkgconfig/"'
                    }
                }
            }
	}
    }
}
