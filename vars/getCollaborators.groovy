def call(String github_url)
{
    // Turn the URL into project/repo
    s = github_url.split("/")
    owner = s[3]    
    repo = s[4].substring(0, s[4].length()-4)
    url = "https://api.github.com/repos/${owner}/${repo}/collaborators"

    println("CC: NOW GOT HERE for ${url}")
    CREDS = credentials("489d4ea3-11c9-40ab-a794-54197b51f081")
    sh 'echo $CREDS'

    collabs = sh (
	script: 'curl -s -u ' + CREDS  + " ${url}",
	returnStdout: true)
    parsed = jsonSlurper.parse(collabs)
    println("$parsed")
    
    println(parsed.login)
    cnum = parsed.login.size()
    println(cnum)
    
    collabs = [];
    for (i=0; i<cnum; i++ ) {
        println(parsed.login[i])
        println(parsed.role_name[i])
        if ((parsed.role_name[i] == "admin") || (parsed.role_name[i] == "write")) {
            println("Adding this one")
            collabs.add(parsed.login[i])
	}
    }
    return collabs

}
