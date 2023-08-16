def call(String cmd, String visible) {
    sh (script: '#!/bin/sh -e\n'+cmd, returnStdout: true)
    echo("${visible}")
}
