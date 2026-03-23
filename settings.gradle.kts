pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
  id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.23"
  id("com.gradle.develocity") version "3.19.2"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    google()
  }
}

buildCache {
  local {
    isEnabled = true
    directory = File(rootProject.projectDir, ".gradle/build-cache")
  }
}

// gitHooks {
//    commitMsg {
//        conventionalCommits {
//            defaultTypes()
//            types("merge")
//        }
//    }
//    createHooks(true)
// }

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
  }
}

rootProject.name = "quarkus-rbac-template"

include("apps:admin-api")

include("libs:common")

include("libs:persistence")

include("libs:accesscontrol")

include("libs:identity")

include("libs:audit")

include("libs:cache")

include("libs:security")

include("apps:migrator")
