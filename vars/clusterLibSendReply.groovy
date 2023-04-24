def call(Map config = [:]) {
    // TODO make this site-agnostic so that it can deal with Pagure as well as github
    if (config.platform == "github") {
	pullRequest.Comment(config.msg);
    } else {
	echo "Platform ${config.platform} not configure in clusterLibSendReply.groovy"
    }
}
