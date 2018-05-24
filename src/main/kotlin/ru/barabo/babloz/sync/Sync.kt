package ru.barabo.babloz.sync

import javafx.scene.control.Alert
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

    lateinit var mailProp: MailProperties

    override fun subjectCriteria() = "Babloz db saved:"

    override fun bablozAttachName() = "babloz.db"

    override fun bablozFilePath(): String = currentDirectory()

    private fun currentDirectory(): String {
        val currentDb = File("$CURRENT_PATH/$BABLOZ_JAR")

        return if(currentDb.exists()) Cmd.JAR_FOLDER else CURRENT_PATH
    }

    private val CURRENT_PATH = "${FileSystems.getDefault().getPath("").toAbsolutePath()}"

    private const val BABLOZ_JAR = "babloz.jar"

    fun startSync(login: String, password: String) {

        logger.error("${FileSystems.getDefault().getPath("").toAbsolutePath()}")

        mailProp = MailProperties(user = login, password = password)

        downloadSyncFile(mailProp)

        val file = this.getDbInMail(mailProp)

        logger.error("fileSync=$file")
    }

    private fun downloadSyncFile(mailProp: MailProperties) {

        val file = try {
           this.getDbInMail(Sync.mailProp)
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

        try {
            sendDb(mailProp)
        } catch (e: Exception) {
            logger.error("endSync", e)

            alert(Alert.AlertType.ERROR, e.message!!)
        }
    }
}