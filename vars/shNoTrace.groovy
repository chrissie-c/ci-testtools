def call(String cmd, String visible) {
    def a = steps.sh (script: '#!/bin/sh -e\n'+cmd, returnStdout: true, label: visible)
    return a
}
