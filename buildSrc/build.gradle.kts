plugins { `kotlin-dsl` }

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
  testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
