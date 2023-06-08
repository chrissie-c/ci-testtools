def call(String url)
{
    // Turn the URL into project/repo
    s = url.split("/")
    owner = s[3]    
    repo = s[4].substring(0,s[4].length()-4)
    url = "https://api.github.com/repos/${owner}/${repo}/collaborators"

    println("CC: GOT HERE for ${url}")
    COLLAB_CREDENTIALS = credentials("489d4ea3-11c9-40ab-a794-54197b51f081")
    println('$COLLAB_CREDENTIALS')

    collabs = sh (
	script: "curl -s -u '${COLLAB_CREDENTIALS}'" + " ${url}",
	returnStdout: true)
    parsed = readJSON text: collabs
    len=5; //yeah
    println("{$collabs}")
    
    String[] collabs;    
    for (i=0; i<len; i++ ) {
	println(parsed.login)
	collabs[i] = parsed.login
    }
    return collabs
}
