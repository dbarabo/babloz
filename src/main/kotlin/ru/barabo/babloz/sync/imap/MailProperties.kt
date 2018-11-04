package ru.barabo.babloz.sync.imap

import java.util.*
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Store

data class MailProperties(
        val hostImap: String = "imap.gmail.com",
        val portImap: Int = 993,
        val hostSmtp: String = "smtp.gmail.com",
        val portSmtp: Int = 587,
        val tlsSmtpEnable: Boolean = true,
        var user: String,
        var password: String) {

    companion object {
        private const val IMAP_PROTOCOL = "imaps"

        internal const val SMTP_PROTOCOL = "smtp"

        const val INBOX = "INBOX"

        const val SENT = "[Gmail]/Отправленные"
    }

    private fun imapProperties(): Properties = Properties().apply {
        put("mail.store.protocol", IMAP_PROTOCOL)

        put("mail.pop3.host", hostImap)
        put("mail.imaps.host", hostImap)

        put("mail.pop3.port", portImap.toString())
        put("mail.imaps.port", portImap.toString())
    }

    private fun authenticator() : Authenticator? {

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

    fun connectImapStore(): Store = connectStore(getImapStore() )


    @Synchronized
    fun smtpSession(): Session = Session.getInstance(smtpProperties(), authenticator() ).apply { debug = false }

    private fun smtpProperties(): Properties = Properties().apply {

        put("mail.transport.protocol", SMTP_PROTOCOL)
        put("mail.smtp.host", hostSmtp)
        put("mail.smtp.auth", tlsSmtpEnable.toString())
        put("mail.smtp.starttls.enable", tlsSmtpEnable.toString())
        put("mail.smtp.port", portSmtp.toString());
        put("mail.smtp.from", user)

        //put("mail.smtp.user", smtpProperties.user)
        //put("mail.smtp.password", smtpProperties.password)
    }
}