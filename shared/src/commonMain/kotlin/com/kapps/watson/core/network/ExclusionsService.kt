package com.kapps.watson.core.network

/**
 * Downloads and parses Sherlock's community-maintained list of false-positive sites.
 *
 * Some sites in data.json return 200 OK for any username (e.g. Reddit, YouTube), which
 * would cause every scan to flag thousands of inexistent accounts. Sherlock mitigates
 * this by maintaining a separate exclusion list at:
 *
 *   https://raw.githubusercontent.com/sherlock-project/sherlock/refs/heads/exclusions/false_positive_exclusions.txt
 *
 * Each line is a site name (matching a key in data.json) that should be filtered out
 * unless the user explicitly opts in (equivalent of --ignore-exclusions in Sherlock CLI).
 */
interface ExclusionsService {

    /**
     * Returns the set of site names to exclude from scans.
     * Returns an empty set on network failure rather than throwing — the worst case
     * is more false positives, not a broken app.
     */
    suspend fun loadExclusions(): Set<String>
}