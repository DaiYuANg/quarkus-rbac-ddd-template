plugins { alias(libs.plugins.jandex) }

dependencies {
  api(libs.jakarta.ws.rs)
  api(libs.jakarta.validation)
  api(libs.jackson.annotations)
}
