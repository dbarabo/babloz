package ru.barabo.babloz.sync

import org.slf4j.LoggerFactory
import ru.barabo.babloz.sync.imap.GetMailDb
import ru.barabo.babloz.sync.imap.MailProperties

object Sync : GetMailDb {

    private val logger = LoggerFactory.getLogger(Sync::class.java)!!

    fun startSync(login: String, password: String) {

        val mailProp = MailProperties(user = login, password = password)

        val file = this.getDbInMail(mailProp)

        logger.error("fileSync=$file")
    }
}