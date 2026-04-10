# buildSrc

[English](README.md)

## replacePackage 任务

批量替换项目中的包名和 group。

### 用法

```bash
./gradlew replacePackage -PfromPackage=<原包名> -PtoPackage=<目标包名>
```

示例：

```bash
./gradlew replacePackage -PfromPackage=com.github.DaiYuANg -PtoPackage=com.example.myproject
```

### 选项

- `-PdryRun=true` — 仅打印将要执行的操作，不修改文件

### 执行内容

1. 替换所有源文件中的包名（java, kt, kts, yaml, md, sql, xml, properties）
2. 更新根项目 `build.gradle.kts` 中的 `group`
3. 重命名 `apps`、`libs`、`modules` 下 `src/main/java`、`src/test/java`、
   `src/main/kotlin`、`src/test/kotlin` 等源码根目录中的包路径

---

## generateRsaKeys 任务

生成 JWT 签名所需的 RSA 密钥对。**首次启动前**需执行，生成的密钥已加入 gitignore。

### 用法

```bash
./gradlew generateRsaKeys
```

默认输出到**项目根目录**。`quarkusDev` 已配置以根目录为工作目录，应用会自动找到密钥。

### 选项

- `-PoutputDir=<路径>` — 输出目录（默认：项目根目录）
- `-PkeySize=<大小>` — RSA 密钥位数（默认：2048）

### 输出

- `privateKey.pem` — RSA 私钥（PKCS#1）
- `publicKey.pem` — RSA 公钥
