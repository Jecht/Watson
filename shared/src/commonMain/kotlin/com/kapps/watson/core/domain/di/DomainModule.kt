package com.kapps.watson.core.domain.di

import com.kapps.watson.core.domain.usecase.*
import org.koin.dsl.module

/**
 * Koin module that provides domain use cases.
 *
 * Use cases are declared as `factory` since they are lightweight and stateless:
 * a new instance per injection is fine and avoids accidental state sharing.
 */
val domainModule = module {
    factory<ValidateUsernameUseCase> { ValidateUsernameUseCaseImpl() }
    factory<BuildProbeUrlUseCase> { BuildProbeUrlUseCaseImpl() }
    factory<DetectWafUseCase> { DetectWafUseCaseImpl() }
    factory<ProbeSiteUseCase> {
        ProbeSiteUseCaseImpl(
            siteProbeService = get(),
            validateUsernameUseCase = get(),
            buildProbeUrlUseCase = get(),
            detectWafUseCase = get(),
        )
    }
    factory<ScanUsernameUseCase> {
        ScanUsernameUseCaseImpl(
            sitesRepository = get(),
            probeSiteUseCase = get(),
        )
    }
}