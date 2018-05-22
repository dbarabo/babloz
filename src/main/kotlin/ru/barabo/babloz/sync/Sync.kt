package ru.barabo.babloz.sync

import org.slf4j.LoggerFactory
import ru.barabo.babloz.sync.imap.GetMailDb
import ru.barabo.babloz.sync.imap.MailProperties
import ru.barabo.babloz.sync.smtp.SendMailDb
import ru.barabo.cmd.Cmd

object Sync : GetMailDb, SendMailDb {

    private val logger = LoggerFactory.getLogger(Sync::class.java)!!

    lateinit var mailProp: MailProperties

    override fun subjectCriteria() = "Babloz db saved:"

    override fun bablozAttachName() = "babloz.db"

    override fun bablozFilePath(): String = Cmd.JAR_FOLDER

    fun startSync(login: String, password: String) {

        mailProp = MailProperties(user = login, password = password)

        val file = this.getDbInMail(mailProp)

        logger.error("fileSync=$file")
    }

    fun endSync() {

        sendDb(mailProp)
    }
}