# 故障排查

## Infinispan: ClassNotFoundException DigestClientFactory（JDK 25）

使用 **JDK 25** 连接 Infinispan Hot Rod 客户端认证时，可能出现：

```
ClassNotFoundException: org.wildfly.security.sasl.digest.DigestClientFactory
```

这是 JDK 25 模块系统与 WildFly Elytron SASL 在 Security Provider 加载时的已知兼容问题。

### 解决方案

**改用 JDK 21**（LTS）进行开发与部署。Quarkus 和 Infinispan 在 JDK 21 上经过充分验证。

1. 安装 JDK 21（如 Eclipse Temurin、Amazon Corretto）。
2. 让项目使用 JDK 21：
   - 编辑 `gradle/libs.versions.toml`，设置 `jdk = "21"`。
   - 或通过 `JAVA_HOME` / Gradle toolchain 指定 JDK 21。
3. 重新构建：`./gradlew clean :apps:admin-api:quarkusDev`

### JDK 25 下的临时方案

若必须使用 JDK 25，可尝试通过 JVM 参数放宽 Security Provider 的模块访问：

1. **在 `gradle.properties`**（或复制后的 `gradle.properties.template`）中：

   ```properties
   org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8 --add-opens=java.base/java.security=ALL-UNNAMED --add-reads=java.base=ALL-UNNAMED
   ```

   此配置作用于 Gradle 本身。**Quarkus 应用进程**需使用第 2 步。

2. **为 quarkusDev / quarkusRun 配置 JVM 参数** — 项目已在 `apps/admin-api/build.gradle.kts` 中为 JDK 25 添加了 `--add-opens` 与 `--add-reads`，用于 Elytron SASL Provider 加载。

3. **或通过环境变量**（作用于运行应用的 JVM）：

   ```bash
   export JDK_JAVA_OPTIONS="--add-opens=java.base/java.security=ALL-UNNAMED --add-reads=java.base=ALL-UNNAMED"
   ./gradlew :apps:admin-api:quarkusDev
   ```

若问题仍存在，建议使用 JDK 21。
