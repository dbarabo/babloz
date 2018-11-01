package ru.barabo.babloz.sync

import javafx.scene.control.Alert
import org.slf4j.LoggerFactory
import ru.barabo.archive.Archive
import ru.barabo.archive.Cmd
import ru.barabo.babloz.db.BablozConnection
import ru.barabo.babloz.sync.imap.GetMailDb
import ru.barabo.babloz.sync.imap.MailProperties
import ru.barabo.babloz.sync.smtp.SendMailDb
import tornadofx.alert
import java.io.File
import java.nio.file.FileSystems

object SyncZip : GetMailDb, SendMailDb {

    override fun subjectCriteria(): String = "Babloz backup:"

    override fun bablozAttachName(): String = "babloz.zip"

    override fun bablozFilePath(): String = currentDirectory()

    private val logger = LoggerFactory.getLogger(SyncZip::class.java)!!

    private var mailProp: MailProperties? = null

    private var syncType: SyncTypes = SyncTypes.NO_SYNC_LOCAL_ONLY

    private fun alertError(error: String): Boolean {
        alert(Alert.AlertType.ERROR, error)

        return false
    }

    /**
     * load file from email
     */
    fun startSync(login: String, password: String, syncType: SyncTypes) {
        mailProp = MailProperties(user = login, password = password)

        this.syncType = syncType

        START_SYNC_PROCESS[syncType]!!.invoke()
    }

    fun endSync() {

        if(SyncLoader.isExistsUpdateSyncData()) {
            START_SYNC_PROCESS[syncType]?.invoke()
        }

        BablozConnection.closeAllSessions()
    }

    private fun currentDirectory(): String {
        val currentDb = File("$CURRENT_PATH/$BABLOZ_JAR")

        return if(currentDb.exists()) Cmd.JAR_FOLDER else CURRENT_PATH
    }

    private val CURRENT_PATH = "${FileSystems.getDefault().getPath("").toAbsolutePath()}"

    private const val BABLOZ_JAR = "babloz.jar"

    private const val BACKUP_FILE_NAME = "babloz.bak"

    private const val NOT_EXISTS_FILE = "_NOT_EXISTS_"

    private const val DB_IN_EMAIL_NOTFOUND = "База данных в эл. почте не найдена"

    private val START_SYNC_PROCESS = mapOf<SyncTypes, ()->Boolean>(
            SyncTypes.SYNC_START_SAVE_LOCAL to ::downloadSyncFile,
            SyncTypes.SYNC_START_DEL_LOCAL to ::downloadSyncFile,
            SyncTypes.NO_SYNC_LOCAL_ONLY to ::startLocalOnly
    )

    private fun downloadSyncFile(): Boolean {

        if(!isSuccessMailPropertySmtp()) return false

        val file = downloadFile() ?: return false

        val isExistsNewData = if(file.name == bablozAttachName()) SyncLoader.loadSyncBackup(file) else true

        return if(isExistsNewData) sendBackup() else true
    }

    private fun downloadFile(): File? {
        return try {
            val db = this.getDbInMail(mailProp!!)

            logger.error("db=$db")

            db?.let { it } ?: alertEmailNotFound()
        } catch (e: Exception) {

            logger.error("downloadSyncFile", e)

            alertError(e.message!!)

            null
        }
    }

    private fun zipFileFullPathName() = "${bablozFilePath()}/${bablozAttachName()}"

    private fun backupFileFullPathName() = BACKUP_FILE_NAME

    private fun sendBackup(): Boolean {

        SyncSaver.toZipBackup(zipFileFullPathName(), backupFileFullPathName())

        logger.error("toZipBackup ok")

        return try {
            sendDb(mailProp!!)

            logger.error("sendDb true")

            true
        } catch (e: Exception) {
            logger.error("sendBackup", e)

            alertError(e.message!!)

            false
        }
    }

    private fun alertEmailNotFound(): File {
        alertError(DB_IN_EMAIL_NOTFOUND)

        return File(NOT_EXISTS_FILE)
    }

    private fun startLocalOnly(): Boolean = true

    private fun isSuccessMailPropertySmtp(): Boolean {

        val valid = mailProp?.let { it.user.isNotEmpty() && it.password.isNotEmpty()  } ?: return false

        return if(valid) isCheckSmtpConnect() else false
    }

    private fun isCheckSmtpConnect(): Boolean {
        return try {
            mailProp?.smtpSession()

            true
        } catch (e: Exception) {

            logger.error("isCheckSmtpConnect", e)

            alertError(e.message!!)

            false
        }
    }
}

private object SyncSaver {

    fun toZipBackup(zipFilePath: String, dataFileName: String) =
            Archive.packToZipStream(zipFilePath, dataFileName, getBackupData().byteInputStream())

    private fun getBackupData(): String = SyncLoader.serviceHashByTableName.values.joinToString("\n") { it.getBackupData() }
}