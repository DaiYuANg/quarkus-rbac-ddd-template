plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(projects.libs.security)
  api(libs.quarkus.arc)
  api(libs.quarkus.infinispan.client)
  api(libs.quarkus.qute)
  api(libs.quarkus.rest)

  annotationProcessor(libs.protostream.processor)
}
