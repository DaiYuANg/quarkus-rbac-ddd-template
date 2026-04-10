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
3. Rename package directories under source roots such as `src/main/java`, `src/test/java`,
   `src/main/kotlin`, and `src/test/kotlin` across `apps`, `libs`, and `modules`

---

## generateRsaKeys Task

Generate RSA key pair for JWT signing. Run **before first start** — keys are gitignored.

### Usage

```bash
./gradlew generateRsaKeys
```

Keys are written to the **project root** by default. `quarkusDev` is configured to use the root as working directory so the app finds them.

### Options

- `-PoutputDir=<path>` — Output directory (default: project root)
- `-PkeySize=<size>` — RSA key size in bits (default: 2048)

### Output

- `privateKey.pem` — RSA private key (PKCS#1)
- `publicKey.pem` — RSA public key
