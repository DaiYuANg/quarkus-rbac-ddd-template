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
3. 重命名目录结构（如 `com/github/DaiYuANg` → `com/example/myproject`）

---

## generateRsaKeys 任务

生成 RSA 密钥对并写入 PEM 文件。

### 用法

```bash
./gradlew generateRsaKeys
```

### 选项

- `-PoutputDir=<路径>` — 输出目录（默认：项目根目录）
- `-PkeySize=<大小>` — RSA 密钥位数（默认：2048）

### 输出

- `privateKey.pem` — RSA 私钥（PKCS#1）
- `publicKey.pem` — RSA 公钥

### 自定义路径示例

```bash
./gradlew generateRsaKeys -PoutputDir=apps/admin-api/src/main/resources
```
