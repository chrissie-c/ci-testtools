


def call()
{
    Jenkins.instance.getAllItems(Run).each {
       	println(it)
    }
}
