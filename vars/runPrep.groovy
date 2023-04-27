def call() {
    sh "env"
    sh 'sh autogen.sh'
    sh 'PKG_CONFIG_PATH=/srv/knet/origin/main/lib/pkgconfig/ ./configure'
}
