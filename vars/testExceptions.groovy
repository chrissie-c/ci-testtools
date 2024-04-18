def call()
{
    try {
        timeout(time: 60, unit: 'MINUTES') {
            sh "sleep 120"
        }
    } catch (err) {
        println(err)
        println(err.getCauses())
	println(err.getCauses()[0])
    }
}
