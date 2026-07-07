package com.kapps.watson.core.repository

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies [restrictToOwnerOnly] locks the desktop catalog cache down to owner-only access.
 *
 * The assertions only run on POSIX filesystems; on Windows (no [PosixFileAttributeView]) the helper
 * is a documented no-op, so the test skips the checks rather than asserting an inapplicable result.
 */
class CatalogFilePermissionsTest {

    private fun posixSupported(file: File): Boolean =
        Files.getFileAttributeView(file.toPath(), PosixFileAttributeView::class.java) != null

    @Test
    fun restrictToOwnerOnly_makesFileReadWriteForOwnerOnly() {
        val dir = createTempDirectory("watson-perms").toFile()
        val file = File(dir, "cache.json").apply { writeText("{}") }

        val applied = restrictToOwnerOnly(file, "rw-------")

        if (!posixSupported(file)) return
        assertTrue(applied, "expected POSIX perms to be applied")
        assertEquals(
            setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
            Files.getPosixFilePermissions(file.toPath()),
        )
    }

    @Test
    fun restrictToOwnerOnly_makesDirAccessibleForOwnerOnly() {
        val dir = createTempDirectory("watson-perms").toFile()

        val applied = restrictToOwnerOnly(dir, "rwx------")

        if (!posixSupported(dir)) return
        assertTrue(applied)
        assertEquals(
            setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
            ),
            Files.getPosixFilePermissions(dir.toPath()),
        )
    }
}
