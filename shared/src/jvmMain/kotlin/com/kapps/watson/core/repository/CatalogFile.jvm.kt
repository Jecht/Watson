package com.kapps.watson.core.repository

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions

/** Per-user app data directory; falls back to the temp dir if the home dir is unknown. */
private fun catalogDir(): File {
    val base = System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir")
    return File(base, ".watson")
}

internal actual fun readCatalogFile(): String? {
    val file = File(catalogDir(), CATALOG_FILE_NAME)
    return file.takeIf { it.exists() }?.readText()
}

internal actual fun writeCatalogFile(rawJson: String) {
    val dir = catalogDir()
    dir.mkdirs()
    // Lock the directory down *before* writing so the file is created inside an owner-only dir:
    // the 0700 directory is the load-bearing guard, even if the file itself is momentarily created
    // with the default umask before we tighten it to 0600 below.
    restrictToOwnerOnly(dir, "rwx------")
    val file = File(dir, CATALOG_FILE_NAME)
    file.writeText(rawJson)
    restrictToOwnerOnly(file, "rw-------")
}

/**
 * Best-effort tightening of [file] to owner-only access; returns whether POSIX perms were applied.
 *
 * The desktop cache lives under the user's home dir, but a default umask leaves it group/world
 * readable on a shared *nix host. The cached catalog is public data today, yet this directory is
 * where any future search history / scanned usernames would be persisted, so we restrict it now.
 * Non-POSIX filesystems (Windows) expose no [PosixFileAttributeView]; there the per-user home
 * directory ACL already restricts access, so we skip rather than fail. Any failure is swallowed on
 * purpose: hardening the cache must never make a cache write throw.
 */
internal fun restrictToOwnerOnly(file: File, permissions: String): Boolean {
    val view = Files.getFileAttributeView(file.toPath(), PosixFileAttributeView::class.java)
        ?: return false
    return runCatching {
        view.setPermissions(PosixFilePermissions.fromString(permissions))
        true
    }.getOrDefault(false)
}
