package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.model.SiteInfo

internal class ValidateUsernameUseCaseImpl : ValidateUsernameUseCase {

    override fun invoke(username: String, site: SiteInfo): Boolean {
        val pattern = site.regexCheck ?: return true
        return Regex(pattern).containsMatchIn(username)
    }
}