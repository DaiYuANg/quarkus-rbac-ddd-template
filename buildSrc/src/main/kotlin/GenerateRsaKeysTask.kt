import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.KeyPairGenerator
import java.security.SecureRandom

/**
 * Gradle task to generate RSA key pair and write to PEM files.
 *
 * Usage:
 *   ./gradlew generateRsaKeys
 *
 * With custom output directory:
 *   ./gradlew generateRsaKeys -PoutputDir=src/main/resources
 *
 * Options:
 *   -PoutputDir=<path>  Output directory (default: project root)
 *   -PkeySize=<size>    RSA key size in bits (default: 2048)
 */
abstract class GenerateRsaKeysTask : DefaultTask() {

  @get:Input
  abstract val outputDir: Property<String>

  @get:Input
  abstract val keySize: Property<Int>

  @get:Input
  abstract val privateKeyFile: Property<String>

  @get:Input
  abstract val publicKeyFile: Property<String>

  init {
    outputDir.convention(
      project.provider {
        project.findProperty("outputDir") as? String ?: project.rootProject.layout.projectDirectory.asFile.absolutePath
      }
    )
    keySize.convention(project.provider { (project.findProperty("keySize") as? String)?.toIntOrNull() ?: 2048 })
    privateKeyFile.convention("privateKey.pem")
    publicKeyFile.convention("publicKey.pem")
  }

  @TaskAction
  fun execute() {
    val outDir = project.file(outputDir.get())
    val size = keySize.get()
    val privateName = privateKeyFile.get()
    val publicName = publicKeyFile.get()

    logger.lifecycle("Generating RSA key pair ($size bits)...")

    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(size, SecureRandom())
    val keyPair = keyPairGenerator.generateKeyPair()

    outDir.mkdirs()

    // Write private key
    val privateKeyPath = outDir.resolve(privateName)
    privateKeyPath.outputStream().writer(Charsets.UTF_8).use { writer ->
      JcaPEMWriter(writer).use { pemWriter ->
        pemWriter.writeObject(keyPair.private)
      }
    }
    logger.lifecycle("  Private key: $privateKeyPath")

    // Write public key
    val publicKeyPath = outDir.resolve(publicName)
    publicKeyPath.outputStream().writer(Charsets.UTF_8).use { writer ->
      JcaPEMWriter(writer).use { pemWriter ->
        pemWriter.writeObject(keyPair.public)
      }
    }
    logger.lifecycle("  Public key:  $publicKeyPath")

    logger.lifecycle("Done!")
  }
}
