package me.stepbystep.projectbenchmark

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.security.Key
import java.security.KeyPairGenerator
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

@State(Scope.Benchmark)
@BenchmarkMode(Mode.All)
@Warmup(iterations = 2)
@Measurement(iterations = 2, batchSize = 2)
@ExperimentalStdlibApi
open class CompareEncryptAlgorithms {
    private val shaDigest = MessageDigest.getInstance("SHA-256")
    private val dataBytes = DATA.encodeToByteArray()

    private val aesEncryptCipher: Cipher
    private val aesDecryptCipher: Cipher

    private val rsaEncryptCipher: Cipher
    private val rsaDecryptCipher: Cipher

    init {
        val aesSecretKey = KeyGenerator.getInstance("AES").let {
            it.init(256)
            it.generateKey()
        }

        aesEncryptCipher = getCipher("AES", Cipher.ENCRYPT_MODE, aesSecretKey)
        aesDecryptCipher = getCipher("AES", Cipher.DECRYPT_MODE, aesSecretKey)

        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val (rsaPublicKey, rsaPrivateKey) = keyPairGenerator.generateKeyPair().let {
            it.public to it.private
        }

        rsaEncryptCipher = getCipher("RSA", Cipher.ENCRYPT_MODE, rsaPublicKey)
        rsaDecryptCipher = getCipher("RSA", Cipher.DECRYPT_MODE, rsaPrivateKey)
    }

    private val aesEncryptedBytes = aesEncryptCipher.doFinal(dataBytes)
    private val rsaEncryptedBytes = rsaEncryptCipher.doFinal(dataBytes)

    @Benchmark
    fun hashSHA256(bh: Blackhole) {
        bh.consume(shaDigest.digest(dataBytes))
    }

    @Benchmark
    fun encryptAES256(bh: Blackhole) {
        bh.consume(aesEncryptCipher.doFinal(dataBytes))
    }

    @Benchmark
    fun decryptAES256(bh: Blackhole) {
        bh.consume(aesDecryptCipher.doFinal(aesEncryptedBytes))
    }

    @Benchmark
    fun encryptRSA(bh: Blackhole) {
        bh.consume(rsaEncryptCipher.doFinal(dataBytes))
    }

    @Benchmark
    fun decryptRSA(bh: Blackhole) {
        bh.consume(rsaDecryptCipher.doFinal(rsaEncryptedBytes))
    }

    private fun getCipher(name: String, mode: Int, secretKey: Key): Cipher =
        Cipher.getInstance(name).also { it.init(mode, secretKey) }

    private companion object {
        private const val DATA = "Hello benchmark!"
    }
}