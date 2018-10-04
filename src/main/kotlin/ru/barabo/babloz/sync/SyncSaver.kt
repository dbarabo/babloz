package ru.barabo.babloz.sync

import ru.barabo.archive.Archive

object SyncSaver {

    fun toZipBackup() = Archive.packToZipStream(inputStream = getBackupData().byteInputStream())

    private fun getBackupData(): String = SyncLoader.serviceHashByTableName.values.joinToString("\n") { it.getBackupData() }
}
