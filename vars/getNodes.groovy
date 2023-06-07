@NonCPS
def call() {
    n = jenkins.model.Jenkins.instance.getNodes()
    println("NODES: ${n}")
}

