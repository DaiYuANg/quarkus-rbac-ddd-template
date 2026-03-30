plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.persistence)
  api(projects.libs.accesscontrol)
  api(libs.quarkus.arc)
  implementation(libs.blaze.persistence.quarkus)
  implementation(libs.mapstruct)
  runtimeOnly(libs.blaze.persistence.hibernate)

  val queryDSLApt = variantOf(libs.querydsl.apt) { classifier(JAKARTA) }
  compileOnly(queryDSLApt)
  annotationProcessor(queryDSLApt)
  annotationProcessor(libs.bundles.persistence.annotation.processor)
  annotationProcessor(libs.mapstruct.processor)
}
