def call(String cmd, String visble) {
    sh (script: '#!/bin/sh -e\n'+cmd, returnStdout: true)
    echo("${visible}")
}
