


def call()
{
    println("in getLogFile")

    def run = currentBuild.rawBuild

    println(run)
    println(run.getArtifactsDir());
}
