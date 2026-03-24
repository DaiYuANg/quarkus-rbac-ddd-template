plugins { alias(libs.plugins.jandex) }

dependencies {
  api(libs.jakarta.ws.rs)
  api(libs.jakarta.validation)
  api(libs.jackson.annotations)
  implementation("io.github.daiyuang:data-model:0.0.1")
  implementation("io.github.daiyuang:web-data-model:0.0.1")
}
