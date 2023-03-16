def call(Map config = [:]) {
  sh "make ${config.makeopts}"
}