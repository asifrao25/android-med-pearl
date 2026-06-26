package com.knowledgepearls.app

object AppBrand {
    const val NAME = "Med Pearls"
    const val BACKUP_FOLDER_NAME = "Med Pearls Backups"
    const val BACKUP_FILE_PREFIX = "MedPearls-Backup-"
}

/**
 * Analytics platform slug sent to `analytics-ingest`.
 * iOS sends `ios` / `ipados`; Android must send `android` so Pearls Admin can segment stats.
 */
object AnalyticsPlatform {
    const val VALUE = "android"
}
