


def call()
{
    println("in getLogFile")
    def r = Jenkins.instance.getAllItems(Job).each {
       	println(it)
    }
}
