package com.example.gesturgaitai.core

/**
 * GesturGait AI - Dynamic Versioning System
 * Format: v[Merges to Main].[Total Branches].[Recent Branch Commits]
 */
object AppVersion {
    // Current Git Stats (Manual sync for now as requested)
    private const val MERGES = 0
    private const val BRANCHES = 5
    private const val COMMITS = 11

    const val VERSION_NAME = "v$MERGES.$BRANCHES.$COMMITS"
    const val FULL_VERSION = "GesturGait AI $VERSION_NAME"
}
