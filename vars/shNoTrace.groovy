def call(String cmd, String visible) {
    def a = sh (script: '#!/bin/sh -e\n'+cmd, label: visible)
    return a
}
