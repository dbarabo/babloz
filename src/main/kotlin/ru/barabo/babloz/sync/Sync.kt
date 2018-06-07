package ru.barabo.babloz.sync

import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import org.slf4j.LoggerFactory
import ru.barabo.babloz.sync.imap.GetMailDb
import ru.barabo.babloz.sync.imap.MailProperties
import ru.barabo.babloz.sync.smtp.SendMailDb
import ru.barabo.cmd.Cmd
import tornadofx.alert
import java.io.File
import java.nio.file.FileSystems

object Sync : GetMailDb, SendMailDb {

    private val logger = LoggerFactory.getLogger(Sync::class.java)!!

    private var mailProp: MailProperties? = null

    private var syncType: SyncTypes = SyncTypes.NO_SYNC_LOCAL_ONLY

    override fun subjectCriteria() = "Babloz db saved:"

    override fun bablozAttachName() = "babloz.db"

    override fun bablozFilePath(): String = currentDirectory()

    private fun currentDirectory(): String {
        val currentDb = File("$CURRENT_PATH/$BABLOZ_JAR")

        return if(currentDb.exists()) Cmd.JAR_FOLDER else CURRENT_PATH
    }

    private val CURRENT_PATH = "${FileSystems.getDefault().getPath("").toAbsolutePath()}"

    private const val BABLOZ_JAR = "babloz.jar"

    fun isSuccessMailPropertySmtp(): Boolean {

        val valid = mailProp?.let { it.user.isNotEmpty() && it.password.isNotEmpty()  } ?: return false

        return if(valid) isCheckSmtpConnect() else false
    }

    private fun isCheckSmtpConnect(): Boolean =
            try {
                mailProp?.smtpSession()

                true
            } catch (e: Exception) {
                false
            }

    fun startSync(login: String, password: String, syncType: SyncTypes) {

        logger.error("${FileSystems.getDefault().getPath("").toAbsolutePath()}")

        mailProp = MailProperties(user = login, password = password)

        this.syncType = syncType

        START_SYNC_PROCESS[syncType]!!.invoke(mailProp!!)
    }

    private val START_SYNC_PROCESS = mapOf<SyncTypes, (MailProperties)->Unit>(
            SyncTypes.SYNC_START_SAVE_LOCAL to ::downloadSyncFile,
            SyncTypes.SYNC_START_DEL_LOCAL to ::downloadSyncFile,
            SyncTypes.NO_SYNC_LOCAL_ONLY to ::startLocalOnly
    )

    private fun startLocalOnly(mailProp: MailProperties) { }

    private fun downloadSyncFile(mailProp: MailProperties) {

        val file = try {
           this.getDbInMail(mailProp)
        } catch (e: Exception) {
            alert(Alert.AlertType.ERROR, e.message!!)

            return
        }

        file?.replaceToMainDb()?: alert(Alert.AlertType.ERROR, DB_IN_EMAIL_NOTFOUND)
    }

    private const val DB_IN_EMAIL_NOTFOUND = "База данных в эл. почте не найдена"

    private fun File.replaceToMainDb() {

        val bablozDb = File("${currentDirectory()}/$name")

        if(bablozDb.exists()) {
            bablozDb.delete()
        }

        copyTo(bablozDb, true)

        if(delete()) {
            parentFile.delete()
        }
    }

    fun endSync() {

        END_SYNC_PROCESS[syncType]!!.invoke(mailProp)
    }

    private val END_SYNC_PROCESS = mapOf<SyncTypes, (MailProperties?)->STATUS>(
            SyncTypes.SYNC_START_SAVE_LOCAL to ::saveDbToMail,
            SyncTypes.SYNC_START_DEL_LOCAL to ::saveDbDelete,
            SyncTypes.NO_SYNC_LOCAL_ONLY to ::endLocalOnly
    )

    fun saveDbToEMail(userName: String,  pswd: String) {
        mailProp = mailProp?.apply {
            this.user = userName
            this.password = pswd
        } ?: MailProperties(user = userName, password = pswd)

        saveDbToEMail()
    }

    fun saveDbToEMail() {
        saveDbToMail(mailProp)
    }

    private fun saveDbToMail(mailProp: MailProperties?): STATUS {
        return try {
            sendDb(mailProp!!)

            STATUS.OK
        } catch (e: Exception) {
            logger.error("endSync", e)

            alert(Alert.AlertType.ERROR, e.message!!)

            STATUS.FAIL
        }
    }

    private fun saveDbDelete(mailProp: MailProperties?): STATUS =
            if(saveDbToMail(mailProp) == STATUS.OK) deleteLocalDb() else questionDelete(mailProp)

    private fun questionDelete(mailProp: MailProperties?): STATUS {

        val alertResult = alert(Alert.AlertType.INFORMATION, "Не удалось синхронизировать БД",
                "Не удалось синхронизировать БД\n Всё-равно удалить локальную БД (данные будут потеряны)",
                ButtonType.YES, ButtonType.NO).showAndWait()

        if(alertResult.isPresent && alertResult.get().buttonData == ButtonBar.ButtonData.YES) {
            deleteLocalDb()
        }

        return STATUS.OK
    }

    private fun deleteLocalDb(): STATUS {
        val bablozDb = File("${currentDirectory()}/${bablozAttachName()}")

        return if(bablozDb.delete()) STATUS.OK else STATUS.FAIL
    }

    private fun endLocalOnly(mailProp: MailProperties?):STATUS = STATUS.OK
}

private enum class STATUS {
    OK,
    FAIL
}