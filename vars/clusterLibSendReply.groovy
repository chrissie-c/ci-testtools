def call(String platform, String msg) {
    // TODO make this site-agnostic so that it can deal with Pagure as well as github
    if (platform == "github") {
	comment = '$msg'
	pullRequest.Comment(comment);
    } else {
	echo "Platform ${platform} not configure in clusterLibSendReply.groovy"
    }
}
