def call(Exception err)
{
    def user = err.getCauses()[0].getUser()
    println("Caught "+err)
    println("user: "+user);
    println("causes: "+err.getCauses());
    println("causes[0]: "+err.getCauses()[0]);
    if('SYSTEM' == user.toString()) {
	println("yeeh, it was SYSTEM")
    }
}
