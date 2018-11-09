package ru.barabo.babloz.sync

import javafx.scene.control.Alert
import org.slf4j.LoggerFactory
import ru.barabo.archive.Archive
import ru.barabo.archive.Cmd
import ru.barabo.babloz.db.BablozConnection
import ru.barabo.babloz.db.service.ProfileService
import ru.barabo.babloz.sync.imap.GetMailDb
import ru.barabo.babloz.sync.imap.MailProperties
import ru.barabo.babloz.sync.imap.ResponseFile
import ru.barabo.babloz.sync.smtp.SendMailDb
import tornadofx.alert
import java.io.File
import java.nio.file.FileSystems
import javax.mail.Store

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

    fun getImapConnect(login: String?, password: String?, syncType: SyncTypes, lastUid: Long = 0): ResponseImap {

        this.syncType = syncType

        if(syncType == SyncTypes.NO_SYNC_LOCAL_ONLY) return ResponseImap.RESPONSE_IMAP_LOCAL

        if(login.isNullOrEmpty() || password.isNullOrEmpty() ) return ResponseImap.RESPONSE_IMAP_CANCEL

        mailProp = MailProperties(user = login!!, password = password!!)

        val store = getImapConnect(mailProp!!)

        return ResponseImap(isSuccess = store != null, imapConnect = store, lastUidSaved = lastUid)
    }

    private fun getImapConnect(mailProp: MailProperties): Store? {
        return try {
            getConnectImap(mailProp)
        } catch (e: Exception) {

            logger.error("getImapConnect", e)

            alertError(e.message!!)

            null
        }
    }

    /**
     * load file from email
     * return last uid message
     */
    fun startSync(responseImap: ResponseImap): Long {

        if(syncType == SyncTypes.NO_SYNC_LOCAL_ONLY) return responseImap.lastUidSaved.apply { responseImap.imapConnect?.close() }

        return responseImap.imapConnect?.let { downloadSyncFile(it, responseImap.lastUidSaved) } ?: responseImap.lastUidSaved
    }

    fun endSync() {

        if(SyncLoader.isExistsUpdateSyncData() ) {
            saveEndBackup()
        }

        BablozConnection.closeAllSessions()
    }

    internal fun saveEndBackup() {
        if(syncType == SyncTypes.NO_SYNC_LOCAL_ONLY) return

        val profile = ProfileService.dataProfile()

        val oldUID = profile.msgUidSync?:0L

        val store = getImapConnect(mailProp!!) ?: return

        val downloadUID = store.use { downloadSyncFile(it, oldUID) }

        profile.msgUidSync = if(downloadUID == oldUID) sendBackup(oldUID) else downloadUID

        ProfileService.save(profile)

        SyncLoader.resetDataAfterSend()
    }

    private fun currentDirectory(): String {
        val currentDb = File("$CURRENT_PATH/$BABLOZ_JAR")

        return if(currentDb.exists()) Cmd.JAR_FOLDER else CURRENT_PATH
    }

    private val CURRENT_PATH = "${FileSystems.getDefault().getPath("").toAbsolutePath()}"

    private const val BABLOZ_JAR = "babloz.jar"

    private const val BACKUP_FILE_NAME = "babloz.bak"

    private fun downloadSyncFile(imapConnect: Store, lastUidSaved: Long): Long {

        val responseFile = downloadFile(imapConnect, lastUidSaved)

        if((!responseFile.isSuccess) || responseFile.file == null) return lastUidSaved

        val isExistsNewData = SyncLoader.loadSyncBackup(responseFile.file)

        return if(isExistsNewData) sendBackup(responseFile.uidMessage) else responseFile.uidMessage
    }

    private fun downloadFile(imapConnect: Store, lastUidSaved: Long): ResponseFile {
        return try {

            downloadFileMail(imapConnect, lastUidSaved)

        } catch (e: Exception) {

            logger.error("downloadSyncFile", e)

            alertError(e.message!!)

            ResponseFile.RESPONSE_FAIL
        }
    }

    private fun zipFileFullPathName() = "${bablozFilePath()}/${bablozAttachName()}"

    private fun sendBackup(oldUID: Long): Long {

        SyncSaver.toZipBackup(zipFileFullPathName(), BACKUP_FILE_NAME)

        return try {
            val attachment = sendDb(mailProp!!)

            //attachment.delete()

            getLastUIDSent(mailProp!!) ?: oldUID
        } catch (e: Exception) {
            logger.error("sendBackup", e)

            alertError(e.message!!)

            oldUID
        }
    }
}

private object SyncSaver {

    fun toZipBackup(zipFilePath: String, dataFileName: String) =
            Archive.packToZipStream(zipFilePath, dataFileName, getBackupData().byteInputStream())

    private fun getBackupData(): String = SyncLoader.serviceHashByTableName.values.joinToString("\n") { it.getBackupData() }
}