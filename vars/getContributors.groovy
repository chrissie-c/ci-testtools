def call(String owner, String repo)
{
    def apiUrl = "https://api.github.com/repos/${owner}/${repo}/collaborators"
    def contributors = []
    
    // Make an HTTP request to the GitHub API
    def response = httpRequest(
        url: apiUrl,
        authentication: 'github',
        validResponseCodes: '200',
        contentType: 'APPLICATION_JSON'
    )
    
    // Parse the JSON response
    def json = readJSON(text: response.content)
    
    // Extract the contributors' usernames
    json.each { contributor ->
        contributors.add(contributor.login)
    }
    
    // Print the list of contributors
    println "Authorized Contributors: ${contributors}"
}
