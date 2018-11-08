package ru.barabo.babloz.sync.imap

import org.slf4j.LoggerFactory
import ru.barabo.archive.Archive
import ru.barabo.babloz.sync.SyncZip
import java.io.File
import javax.mail.*
import javax.mail.internet.MimeBodyPart
import javax.mail.search.*

data class ResponseFile(val file: File? = null, val isSuccess: Boolean = true, val uidMessage: Long = 0) {
    companion object {
        fun equalUidSuccess(uid: Long = 0L): ResponseFile = ResponseFile(file = null, isSuccess = true, uidMessage = uid)

        val RESPONSE_FAIL = ResponseFile(isSuccess = false)
    }
}

interface GetMailDb {

    fun subjectCriteria(): String

    fun bablozAttachName(): String

    fun getConnectImap(mailProp: MailProperties): Store? = mailProp.connectImapStore()

    /**
     * @return db-file for mail inbox
     */
    fun downloadFileMail(imapConnect: Store, lastUidSaved: Long): ResponseFile {

        return imapConnect.use {

            val searchTerm = getSearchTerm(imapConnect.urlName.username)

            val folderSent = imapConnect.getFolder(MailProperties.SENT)

            val result = folderSent.getResponseFileLastMessage(searchTerm, lastUidSaved)

            if(result.isSuccess && (lastUidSaved != result.uidMessage) && (result.uidMessage != 0L)) {
                val folderInbox = imapConnect.getFolder(MailProperties.INBOX)

                folderInbox.dropAllBackup(searchTerm)
            }

            result
        }
    }

    fun getLastUIDSent(mailProp: MailProperties): Long? {

        val store = mailProp.connectImapStore()

        return store.use {
            val searchTerm = getSearchTerm(it.urlName.username)

            val folderSent = it.getFolder(MailProperties.SENT)

            folderSent.getLastMessage(searchTerm)
        }
    }

    private fun Folder.getLastMessage(searchTerm: SearchTerm): Long? {
        open(Folder.READ_ONLY)

        return use {
            val messages = it.search(searchTerm) ?: return null

            val lastMessage = messages.getLastMessage() ?: return null

            (it as? UIDFolder)?.getUID(lastMessage)
        }
    }

    private fun Folder.getResponseFileLastMessage(searchTerm: SearchTerm, lastUidSaved: Long): ResponseFile {
        open(Folder.READ_WRITE)

        return use {
            val messages = it.search(searchTerm) ?: return ResponseFile.equalUidSuccess(lastUidSaved)

            messages.forEach { LoggerFactory.getLogger(GetMailDb::class.java).error( it.receivedDate?.time?.toString() ) }

            val lastMessage = messages.getLastMessage()?: return ResponseFile.equalUidSuccess(lastUidSaved)

            LoggerFactory.getLogger(GetMailDb::class.java).error("lastMessage=${lastMessage.receivedDate?.time}")

            val uid =  (it as? UIDFolder)?.getUID(lastMessage)

            LoggerFactory.getLogger(GetMailDb::class.java).error("uid=$uid")

            LoggerFactory.getLogger(GetMailDb::class.java).error("lastUidSaved=$lastUidSaved")

            if(uid == lastUidSaved) return ResponseFile.equalUidSuccess(lastUidSaved)

            messages.dropMessagesWithout(lastMessage)

            lastMessage.getAttachUID(it)
        }
    }

    private fun Message.getAttachUID(folder: Folder): ResponseFile {

        val file = getAttachDb()?.downloadFile()

        return file?.let {
            val uid = (folder as? UIDFolder)?.getUID(this) ?: 0L

            ResponseFile(it, true, uid)
        } ?: ResponseFile(isSuccess = false)
    }

    private fun Folder.dropAllBackup(searchTerm: SearchTerm) {
        open(Folder.READ_WRITE)

        use {
            search(searchTerm)?.forEach { msg -> msg.setFlag(Flags.Flag.DELETED, true) }
        }
    }

    private fun Array<out Message>.dropMessagesWithout(without: Message?) {
        asSequence().filter { it != without }.forEach {
            it.setFlag(Flags.Flag.DELETED, true)
        }
    }

    private fun Array<out Message>.getLastMessage(): Message? =
            asSequence().maxWith( compareBy { it.receivedDate?.time ?: Long.MIN_VALUE } )

     private fun getSearchTerm(userName: String): SearchTerm =
            AndTerm(arrayOf(FromStringTerm(userName),
                    RecipientStringTerm(Message.RecipientType.TO, userName),
                    SubjectTerm(subjectCriteria() )) )


    private fun Message.getAttachDb(): MimeBodyPart? {

        if(content !is Multipart) return null

        val multiPart = content as Multipart

        for (indexPart in 0 until multiPart.count) {

            val mimePart = multiPart.getBodyPart(indexPart) as? MimeBodyPart ?: continue

            if(!Part.ATTACHMENT.equals(mimePart.disposition, true)) continue

            if(bablozAttachName() == mimePart.fileName) return mimePart
        }

        return null
    }

    private fun MimeBodyPart.downloadFile(): File {

        val file = File("${Archive.tempFolder()}/${bablozAttachName()}")

        this.saveFile(file)

        return file
    }
}