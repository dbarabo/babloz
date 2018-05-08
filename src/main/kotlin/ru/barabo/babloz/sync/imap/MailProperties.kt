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

        const val INBOX = "INBOX"
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

    fun connectImapStore(): Store {

        val store =  getImapStore()

        store.connect(user, password)

        return store
    }
}