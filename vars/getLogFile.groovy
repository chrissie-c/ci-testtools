


def call()
{
    println("in getLogFile")
    Jenkins.instance.getAllItems(Run).each {
       	println(it)
    }
}
