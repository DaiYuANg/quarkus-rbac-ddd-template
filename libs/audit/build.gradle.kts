plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.persistence)
  api(projects.libs.security)
  api(libs.quarkus.arc)

  val queryDSLApt = variantOf(libs.querydsl.apt) { classifier(JAKARTA) }
  compileOnly(queryDSLApt)
  annotationProcessor(queryDSLApt)
  annotationProcessor(libs.bundles.persistence.annotation.processor)
}
