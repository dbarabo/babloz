package ru.barabo.babloz.sync.imap

import org.slf4j.LoggerFactory
import ru.barabo.archive.Archive
import java.io.File
import javax.mail.*
import javax.mail.internet.MimeBodyPart
import javax.mail.search.*

interface GetMailDb {


    fun subjectCriteria(): String

    fun bablozAttachName(): String

    /**
     * @return db-file for mail inbox
     */
    fun getDbInMail(mailProp: MailProperties): File? {

        val store = mailProp.connectImapStore()

        val file = try {

            val folder = store.getFolder(MailProperties.INBOX)

            val attachPart = folder.findLastMessageDb(mailProp)

            attachPart?.downloadFile()

        } catch (e: Exception) {
            LoggerFactory.getLogger(GetMailDb::class.java).error("getDbInMail", e)

            store.close()

            throw MessagingException(e.message)
        }

        store.close()

        return file
    }

    private fun Folder.findLastMessageDb(mailProp: MailProperties): MimeBodyPart? {
        open(Folder.READ_ONLY /*Folder.READ_WRITE*/)

        val searchTerm = getSearchTerm(mailProp)

        val message = try {
           val messages = search(searchTerm)

            messages.findLastMessageWithAttach()
        } catch (e: Exception) {
            LoggerFactory.getLogger(GetMailDb::class.java).error("findLastMessageDb", e)

            close(false)

            throw MessagingException(e.message)
        }

        close(false)

        return message
    }

    private fun getSearchTerm(mailProp: MailProperties): SearchTerm =
            AndTerm(arrayOf(FromStringTerm(mailProp.user),
                    RecipientStringTerm(Message.RecipientType.TO, mailProp.user),
                    SubjectTerm(subjectCriteria() )) )


    private fun Array<Message>.findLastMessageWithAttach(): MimeBodyPart? {

        var lastTime: Long = Long.MIN_VALUE
        var lastPart: MimeBodyPart? = null


        for (message in this) {

            val part =  message.getAttachDb() ?: continue

            if(message.receivedDate?.time?:Long.MIN_VALUE > lastTime) {

                lastTime = message.receivedDate.time

                lastPart = part
            }
        }
        return lastPart
    }

    private fun Message.getAttachDb(): MimeBodyPart? {

        if(content !is Multipart) return null

        val multiPart = content as Multipart

        for (indexPart in 0 until multiPart.count) {

            val mimePart = multiPart.getBodyPart(indexPart) as? MimeBodyPart ?: continue

            if(!Part.ATTACHMENT.equals(mimePart.disposition, true)) continue

            if(bablozAttachName().equals(mimePart.fileName)) return mimePart
        }
        return null
    }

    private fun MimeBodyPart.downloadFile(): File {

        val file = File("${Archive.tempFolder()}/${bablozAttachName()}")

        this.saveFile(file)

        return file
    }
}

