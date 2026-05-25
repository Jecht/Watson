package com.kapps.watson.core.domain.usecase

internal class DetectWafUseCaseImpl : DetectWafUseCase {

    private val fingerprints = listOf(
        // Cloudflare challenge page, 2024-05-13
        ".loading-spinner{visibility:hidden}body.no-js .challenge-running{display:none}body.dark{background-color:#222;color:#d9d9d9}body.dark a{color:#fff}body.dark a:hover{color:#ee730a;text-decoration:underline}body.dark .lds-ring div{border-color:#999 transparent transparent}body.dark .font-red{color:#b20f03}body.dark",

        // Cloudflare error page, 2024-11-11
        "<span id=\"challenge-error-text\">",

        // AWS WAF / CloudFront, 2024-11-11
        "AwsWafIntegration.forceRefreshToken",

        // PerimeterX / Human Security, 2024-04-09
        "{return l.onPageView}}),Object.defineProperty(r,\"perimeterxIdentifiers\",{enumerable:",
    )

    override fun invoke(responseBody: String): Boolean =
        fingerprints.any { fingerprint -> fingerprint in responseBody }
}