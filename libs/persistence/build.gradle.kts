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
  api(libs.blaze.persistence.querydsl) {
    exclude(group = "com.querydsl", module = "querydsl-jpa")
    exclude(group = "com.querydsl", module = "querydsl-core")
  }
  runtimeOnly(libs.blaze.persistence.hibernate)

  implementation("io.github.daiyuang:hibernate-snowflake-id:0.0.1")

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
