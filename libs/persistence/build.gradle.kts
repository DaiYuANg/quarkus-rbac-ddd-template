plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(projects.libs.security)
  api(libs.quarkus.arc)
  api(libs.quarkus.hibernate.orm)
  api(libs.quarkus.hibernate.orm.panache)
  api(libs.quarkus.jdbc.postgresql)
  api(libs.quarkus.agroal)
  api(libs.quarkus.hibernate.validator)
  api(libs.quarkus.rest.jackson)

  implementation(libs.blaze.persistence.quarkus)
  runtimeOnly(libs.blaze.persistence.hibernate)


  api(libs.querydsl.core)
  api(libs.querydsl.jpa)
  api(libs.querydsl.collection)
  api(libs.querydsl.spatial)
  api(libs.querydsl.guava)

  val queryDSLApt = variantOf(libs.querydsl.apt) { classifier(JAKARTA) }
  compileOnly(queryDSLApt)
  annotationProcessor(queryDSLApt)
  annotationProcessor(libs.bundles.persistence.annotation.processor)
}
