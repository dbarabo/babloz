package ru.barabo.babloz.sync.smtp

import ru.barabo.babloz.sync.imap.MailProperties
import java.io.File
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.*

interface SendMailDb {

    fun subjectCriteria(): String

    fun bablozAttachName(): String

    fun bablozFilePath(): String

    fun sendDb(mailProp: MailProperties): File {

        val smtpSession = mailProp.smtpSession()

        val message = smtpSession.createMessage(mailProp.user)

        val attachment = message.addMultiPartWithAttachmentDb()

        smtpSession.messageSend(message, mailProp)

        return attachment
    }

    private fun charsetSubject() = "UTF-8"

    @Synchronized
    private fun Session.createMessage(fromToUserMail: String): MimeMessage {

        val message = MimeMessage(this)

        message.setSubject(subjectCriteria(), charsetSubject() )

        message.setFrom(InternetAddress(fromToUserMail))

        message.addRecipient(Message.RecipientType.TO, InternetAddress(fromToUserMail))

        return message
    }

    private fun Session.messageSend(message: MimeMessage, mailProp: MailProperties) {

        val transport = getTransport(MailProperties.SMTP_PROTOCOL)

        transport.connect(mailProp.hostSmtp, mailProp.user, mailProp.password)

        transport.sendMessage(message, message.allRecipients)

        transport.close()
    }

    private fun MimeMessage.addMultiPartWithAttachmentDb(): File {

        val multipart = MimeMultipart("mixed")

        multipart.addEmptyTextPart()

        val result = multipart.addAttachmentBabloz()

        setContent(multipart)

        return result
    }

    private fun MimeMultipart.addAttachmentBabloz(): File {

        val bablozFile = File("${bablozFilePath()}/${bablozAttachName()}")

        val attachPart = MimeBodyPart()

        attachPart.dataHandler = DataHandler( FileDataSource(bablozFile) )

        attachPart.fileName = MimeUtility.encodeText(bablozFile.name, "utf-8", "Q")

        attachPart.setHeader("Content-Type",
                "application/octet-stream; name=" + MimeUtility.encodeText(bablozFile.name, "utf-8", "Q"))

        attachPart.setHeader("Content-Disposition",
                "attachment; filename=" + MimeUtility.encodeText(bablozFile.name, "utf-8", "Q"))

        addBodyPart(attachPart)

        return bablozFile
    }

    private fun MimeMultipart.addEmptyTextPart() {

        val textBodyPart = MimeBodyPart()
        textBodyPart.setContent("", "text/plain; charset=utf-8")

        addBodyPart(textBodyPart)
    }
}

