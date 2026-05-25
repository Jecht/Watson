package com.kapps.watson.core.domain.usecase

import io.ktor.http.*

internal class BuildProbeUrlUseCaseImpl : BuildProbeUrlUseCase {

    override fun invoke(urlPattern: String, username: String): String {
        val usernamePlaceholder = "{}"
        val encoded = username.encodeURLPath()
        return urlPattern.replace(usernamePlaceholder, encoded)
    }
}