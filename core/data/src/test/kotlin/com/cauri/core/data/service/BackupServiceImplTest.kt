package com.cauri.core.data.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.security.SecureRandom
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BackupServiceImplTest {
    // Few iterations: the tests check the format, not the strength of the key.
    private val service = BackupServiceImpl(iterations = 1_000, random = SecureRandom())

    @Nested
    inner class Read {
        @Test
        fun `should give back the files with the passphrase they were locked with`() {
            // given
            val file = service.write(FILES, "kribi plage 2026".toCharArray())

            // when
            val opened = service.read(file, "kribi plage 2026".toCharArray())

            // then
            opened.shouldBeInstanceOf<OpenedBackup.Opened>()
            opened.files.mapValues { it.value.decodeToString() } shouldBe mapOf("cauri.db" to "db", "prefs" to "p")
        }

        @Test
        fun `should refuse another passphrase`() {
            // given
            val file = service.write(FILES, "kribi plage 2026".toCharArray())

            // when & then
            service.read(file, "kribi plage 2025".toCharArray()) shouldBe OpenedBackup.WrongPassphrase
        }

        @Test
        fun `should refuse a file changed by one byte`() {
            // given
            val file = service.write(FILES, "kribi plage 2026".toCharArray())

            // when
            file[10] = (file[10] + 1).toByte()

            // then
            service.read(file, "kribi plage 2026".toCharArray()) shouldBe OpenedBackup.WrongPassphrase
        }

        @Test
        fun `should tell a file that is no backup`() {
            // when & then
            service.read("hello, this is a photo".toByteArray() + ByteArray(64), "x".toCharArray()) shouldBe
                OpenedBackup.NotABackup
        }
    }

    @Nested
    inner class Write {
        @Test
        fun `should never write the content readable`() {
            // when
            val file = service.write(mapOf("cauri.db" to "Boulangerie Saker".toByteArray()), "kribi".toCharArray())

            // then
            file.decodeToString().contains("Boulangerie") shouldBe false
        }
    }

    private companion object {
        val FILES = mapOf("cauri.db" to "db".toByteArray(), "prefs" to "p".toByteArray())
    }
}
