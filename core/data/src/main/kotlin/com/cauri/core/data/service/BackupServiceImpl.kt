package com.cauri.core.data.service

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation for [BackupService]. A file is « CAURI » and a format version, the iterations of the key
 * derivation, the salt, the nonce, then the encrypted zip; everything before the zip is authenticated with it.
 */
@Singleton
internal class BackupServiceImpl(private val iterations: Int, private val random: SecureRandom) : BackupService {
    @Inject
    constructor() : this(iterations = ITERATIONS, random = SecureRandom())

    override fun write(files: Map<String, ByteArray>, passphrase: CharArray): ByteArray {
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val nonce = ByteArray(NONCE_SIZE).also(random::nextBytes)
        val header = ByteBuffer.allocate(HEADER_SIZE)
            .put(MAGIC)
            .put(VERSION)
            .putInt(iterations)
            .put(salt)
            .put(nonce)
            .array()
        val cipher = cipher(Cipher.ENCRYPT_MODE, passphrase, salt, nonce, iterations).apply { updateAAD(header) }
        return header + cipher.doFinal(zip(files))
    }

    override fun read(file: ByteArray, passphrase: CharArray): OpenedBackup {
        val header = header(file) ?: return OpenedBackup.NotABackup
        val cipher = cipher(Cipher.DECRYPT_MODE, passphrase, header.salt, header.nonce, header.rounds)
            .apply { updateAAD(file, 0, HEADER_SIZE) }
        return try {
            OpenedBackup.Opened(unzip(cipher.doFinal(file, HEADER_SIZE, file.size - HEADER_SIZE)))
        } catch (_: AEADBadTagException) {
            OpenedBackup.WrongPassphrase
        }
    }

    // The header of [file]; `null` when it is no backup of Cauri.
    private fun header(file: ByteArray): Header? {
        if (file.size <= HEADER_SIZE + TAG_BITS / Byte.SIZE_BITS) return null
        val buffer = ByteBuffer.wrap(file)
        val magic = ByteArray(MAGIC.size).also { buffer.get(it) }
        val version = buffer.get()
        val rounds = buffer.int
        val header = Header(
            rounds = rounds,
            salt = ByteArray(SALT_SIZE).also { buffer.get(it) },
            nonce = ByteArray(NONCE_SIZE).also { buffer.get(it) },
        )
        return header.takeIf { magic.contentEquals(MAGIC) && version == VERSION && rounds in 1..MAX_ITERATIONS }
    }

    private fun cipher(mode: Int, passphrase: CharArray, salt: ByteArray, nonce: ByteArray, rounds: Int): Cipher {
        val spec = PBEKeySpec(passphrase, salt, rounds, KEY_BITS)
        val key = try {
            SecretKeyFactory.getInstance(KEY_DERIVATION).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        return Cipher.getInstance(CIPHER).apply {
            init(mode, SecretKeySpec(key, KEY_ALGORITHM), GCMParameterSpec(TAG_BITS, nonce))
        }
    }

    private fun zip(files: Map<String, ByteArray>): ByteArray = ByteArrayOutputStream().also { bytes ->
        ZipOutputStream(bytes).use { zip ->
            files.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content)
                zip.closeEntry()
            }
        }
    }.toByteArray()

    private fun unzip(bytes: ByteArray): Map<String, ByteArray> = buildMap {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            generateSequence { zip.nextEntry }.forEach { put(it.name, zip.readBytes()) }
        }
    }

    // What a file's header holds before its encrypted zip.
    private class Header(val rounds: Int, val salt: ByteArray, val nonce: ByteArray)

    private companion object {
        val MAGIC = "CAURI".toByteArray()
        const val VERSION: Byte = 1

        // OWASP's advice for PBKDF2 with HMAC-SHA256, a second or so on an entry-level phone.
        const val ITERATIONS = 600_000

        // A file asking for more would make the phone work for minutes: it is no backup of Cauri.
        const val MAX_ITERATIONS = 10_000_000
        const val SALT_SIZE = 16
        const val NONCE_SIZE = 12
        const val TAG_BITS = 128
        const val KEY_BITS = 256
        val HEADER_SIZE = MAGIC.size + 1 + Int.SIZE_BYTES + SALT_SIZE + NONCE_SIZE
        const val KEY_DERIVATION = "PBKDF2WithHmacSHA256"
        const val KEY_ALGORITHM = "AES"
        const val CIPHER = "AES/GCM/NoPadding"
    }
}
