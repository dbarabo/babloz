package ru.barabo.babloz.sync

import org.slf4j.LoggerFactory
import ru.barabo.archive.Archive

object SyncLoader {

    private val logger = LoggerFactory.getLogger(SyncLoader::class.java)

    fun fromZipBackup() {
        val text = Archive.unpackFromZipToString()

        text.lines().size
    }
}