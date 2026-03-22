# buildSrc

[中文](README.zh-CN.md)

## replacePackage Task

Batch-replace package names and group in the project.

### Usage

```bash
./gradlew replacePackage -PfromPackage=<from> -PtoPackage=<to>
```

Example:

```bash
./gradlew replacePackage -PfromPackage=com.github.DaiYuANg -PtoPackage=com.example.myproject
```

### Options

- `-PdryRun=true` — Print actions only, do not modify files

### What it does

1. Replace package in all source files (java, kt, kts, yaml, md, sql, xml, properties)
2. Update `group` in root `build.gradle.kts`
3. Rename directory structure (e.g. `com/github/DaiYuANg` → `com/example/myproject`)

---

## generateRsaKeys Task

Generate RSA key pair and write to PEM files.

### Usage

```bash
./gradlew generateRsaKeys
```

### Options

- `-PoutputDir=<path>` — Output directory (default: project root)
- `-PkeySize=<size>` — RSA key size in bits (default: 2048)

### Output

- `privateKey.pem` — RSA private key (PKCS#1)
- `publicKey.pem` — RSA public key

### Example with custom path

```bash
./gradlew generateRsaKeys -PoutputDir=apps/admin-api/src/main/resources
```
