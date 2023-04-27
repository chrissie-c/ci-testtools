def call() {
    sh 'make distcheck DISTCHECK_CONFIGURE_FLAGS="PKG_CONFIG_PATH=/srv/knet/origin/main/lib/pkgconfig/"'
}
