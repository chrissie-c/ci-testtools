def call(String cmd, String visible) {
    def a = sh (script: '#!/bin/sh -e\n'+cmd, returnStdout: true)
    echo("${visible}")
    return a
}
