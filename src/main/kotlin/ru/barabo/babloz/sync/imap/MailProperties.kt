package ru.barabo.babloz.sync.imap

import java.util.*
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Store

data class MailProperties(
        var hostImap: String,
        var portImap: Int,
        var hostSmtp: String,
        var portSmtp: Int,
        var tlsSmtpEnable: Boolean,
        var isSSL: Boolean,
        var user: String,
        var password: String) {

    companion object {
        private const val IMAP_PROTOCOL = "imaps"

        internal const val SMTP_PROTOCOL = "smtp"

        const val INBOX = "INBOX"

        private const val SENT_GMAIL = "[Gmail]/Отправленные"

        private const val SENT_IMAP = "SENT"

        private const val SENT_YANDEX = "Отправленные"

        private const val SENT_RAMBLER = "SentBox"

        fun sentFolderNameByServer(login: String): String {
            val server = login.substringAfterLast('@', "!").trim().lowercase(Locale.getDefault())

            return when  {
                isGmail(server) -> SENT_GMAIL
                isCockLi(server) -> INBOX
                (server == "!") || isYandex(server) -> SENT_YANDEX
                isRambler(server) -> SENT_RAMBLER
                else -> SENT_IMAP
            }
        }

        fun tryDefineProperties(login: String, password: String): MailProperties? {
            val server = login.substringAfterLast('@').trim().lowercase(Locale.getDefault())

            return when  {
            isGmail(server) -> gmailProperties(login, password)
            isCockLi(server) -> cockLiProperties(login, password)
            isYandex(server) -> yandexProperties(login, password)
            isRambler(server) ->   ramblerProperties(login, password)
                else -> null
            }
        }

        private fun isGmail(serverName: String) = ("gmail.com" == serverName)

        private fun isYandex(serverName: String) = ("yandex.ru" == serverName)

        private fun isRambler(serverName: String) = ("rambler.ru" == serverName)

        private fun isCockLi(serverName: String) = arrayOf("cock.li","airmail.cc","8chan.co","redchan.it",
                "420blaze.it","aaathats3as.com","cumallover.me","dicksinhisan.us",
                "loves.dicksinhisan.us","wants.dicksinhisan.us","dicksinmyan.us",
                "loves.dicksinmyan.us","wants.dicksinmyan.us","goat.si","horsefucker.org",
                "national.shitposting.agency","nigge.rs","tfwno.gf","cock.lu","cock.email",
                "firemail.cc","getbackinthe.kitchen","memeware.net","cocaine.ninja","waifu.club",
                "rape.lol","nuke.africa").contains(serverName)

        private fun gmailProperties(user: String, password: String) =
                MailProperties(
                        hostImap = "imap.gmail.com",
                        portImap = 993,
                        hostSmtp = "smtp.gmail.com",
                        portSmtp = 587,
                        tlsSmtpEnable = true,
                        isSSL = false,
                        user = user,
                        password = password)

        private fun cockLiProperties(user: String, password: String) =
                MailProperties(
                        hostImap = "mail.cock.li",
                        portImap = 993,
                        hostSmtp = "mail.cock.li",
                        portSmtp = 587,
                        tlsSmtpEnable = true,
                        isSSL = false,
                        user = user,
                        password = password)


        private fun yandexProperties(user: String, password: String) =
            MailProperties(
                hostImap = "imap.yandex.ru",
                portImap = 993,
                hostSmtp = "smtp.yandex.ru",
                portSmtp = 465,
                tlsSmtpEnable = true,
                isSSL = true,
                user = user/*.substringBefore('@')*/,
                password = password)

        private fun ramblerProperties(user: String, password: String) =
            MailProperties(
                hostImap = "imap.rambler.ru",
                portImap = 993,
                hostSmtp = "smtp.rambler.ru",
                portSmtp = 465,
                tlsSmtpEnable = true,
                isSSL = true,
                user = user/*.substringBefore('@')*/,
                password = password)
    }

    private fun imapProperties(): Properties = Properties().apply {
        put("mail.store.protocol", IMAP_PROTOCOL)

        put("mail.pop3.host", hostImap)
        put("mail.imaps.host", hostImap)

        put("mail.pop3.port", portImap.toString())
        put("mail.imaps.port", portImap.toString())
    }

    private fun authenticator() : Authenticator {

        return object : Authenticator() {
            override fun getPasswordAuthentication() : PasswordAuthentication =
                    PasswordAuthentication(user, password)
        }
    }

    @Synchronized
    private fun imapSession(): Session = Session.getInstance(imapProperties(), authenticator() ).apply { debug = false }

    private fun getImapStore(): Store = imapSession().getStore(IMAP_PROTOCOL)

    private fun connectStore(store: Store): Store {

        store.connect(user, password)

        return store
    }

    // private val logger = LoggerFactory.getLogger(MailProperties::class.java)

    fun connectImapStore(): Store {

        return connectStore(getImapStore() )
    }


    @Synchronized
    fun smtpSession(): Session = Session.getInstance(smtpProperties(), authenticator() ).apply { debug = false }

    private fun smtpProperties(): Properties = Properties().apply {

        put("mail.transport.protocol", SMTP_PROTOCOL)
        put("mail.smtp.host", hostSmtp)
        put("mail.smtp.auth", tlsSmtpEnable.toString())
        put("mail.smtp.starttls.enable", tlsSmtpEnable.toString())
        put("mail.smtp.port", portSmtp.toString())
        put("mail.smtp.from", user)

        if(isSSL) {
            put("mail.smtp.ssl.enable", "true")
        }
    }
}