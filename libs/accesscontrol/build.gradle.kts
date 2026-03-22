plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.persistence)
  api(libs.quarkus.arc)
  implementation(libs.blaze.persistence.quarkus)
  runtimeOnly(libs.blaze.persistence.hibernate)

  val queryDSLApt = variantOf(libs.querydsl.apt) { classifier(JAKARTA) }
  compileOnly(queryDSLApt)
  annotationProcessor(queryDSLApt)
  annotationProcessor(libs.bundles.persistence.annotation.processor)
}
