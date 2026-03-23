# Troubleshooting

## Infinispan: ClassNotFoundException DigestClientFactory (JDK 25)

When using **JDK 25** with Infinispan Hot Rod client authentication, you may see:

```
ClassNotFoundException: org.wildfly.security.sasl.digest.DigestClientFactory
```

This is a known compatibility issue between JDK 25's module system and WildFly Elytron SASL providers during Security Provider loading.

### Solution

**Use JDK 21** (LTS) for development and production. Quarkus and Infinispan are well-tested with JDK 21.

1. Install JDK 21 (e.g. Eclipse Temurin, Amazon Corretto).
2. Point the project to JDK 21:
   - Edit `gradle/libs.versions.toml`: set `jdk = "21"`.
   - Or use `JAVA_HOME` / Gradle toolchain to run with JDK 21.
3. Rebuild: `./gradlew clean :apps:admin-api:quarkusDev`

### Workaround for JDK 25

If you must use JDK 25, try adding JVM arguments to relax module access for Security Provider loading:

1. **In `gradle.properties`** (or `gradle.properties.template` after copying):

   ```properties
   org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8 --add-opens=java.base/java.security=ALL-UNNAMED --add-reads=java.base=ALL-UNNAMED
   ```

   This affects Gradle itself. For the **Quarkus app process**, use step 2.

2. **JVM args for quarkusDev / quarkusRun** — in `apps/admin-api/build.gradle.kts`:

   ```kotlin
   tasks.named<io.quarkus.gradle.tasks.QuarkusDev>("quarkusDev") {
     workingDirectory.set(rootProject.layout.projectDirectory.asFile)
     jvmArgs = listOf(
       "--add-opens=java.base/java.security=ALL-UNNAMED",
       "--add-reads=java.base=ALL-UNNAMED"
     )
   }
   ```

   For `quarkusRun`, add the same `jvmArgs` to the `QuarkusRun` task if needed.

3. **Or via environment variable** (applies to the JVM that runs the app):

   ```bash
   export JDK_JAVA_OPTIONS="--add-opens=java.base/java.security=ALL-UNNAMED --add-reads=java.base=ALL-UNNAMED"
   ./gradlew :apps:admin-api:quarkusDev
   ```

If the error persists, using JDK 21 is the most reliable fix.
