def printWithNoTrace(String cmd, String visble) {
    step.sh (script: '#!/bin/sh -e\n'+ cmd,returnStdout: true)
    echo visible
}
