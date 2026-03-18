


def call()
{
    println("in getLogFile")

    def root = currentBuild.rawBuild.getRootDir().getAbsolutePath()


    def jobsdir1 = root.substring(0, root.lastIndexOf('/', root.lastIndexOf('/')-1)) // take it back to "builds"
    def jobsdir2 = root.substring(0, root.lastIndexOf('/', root.lastIndexOf('/')-1)) // take it back to "brances"

    println(jobsdir1)
    println(jobsdir2)
}

