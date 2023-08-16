def printWithNoTrace(cmd, String visble) {
    step.sh (script: '#!/bin/sh -e\n'+cmd, returnStdout: true)
    echo visible
}
