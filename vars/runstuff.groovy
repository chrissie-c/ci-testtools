def call(Map config = [:]) {
  sh "echo Prep to run pipeline for ${config.project}, branch ${config.branch}, opts: ${config.makeopts}"
  sh "make ${config.makeopts}"
}