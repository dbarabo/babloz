package ru.barabo.babloz.sync.imap

import ru.barabo.archive.Archive
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

            val searchTerm = getSearchTerm(it.urlName.username)

            val folderSent = it.getSentFolder()

            var result = folderSent.getResponseFileLastMessage(searchTerm, lastUidSaved)

            if(result.file == null && result.uidMessage == 0L) {
                result = it.getFolder(MailProperties.INBOX).getResponseFileLastMessage(searchTerm, lastUidSaved)
            }

            if(result.isSuccess && (lastUidSaved != result.uidMessage) && (result.uidMessage != 0L)) {
                val folderInbox = it.getFolder(MailProperties.INBOX)

                if(folderSent.fullName != folderInbox.fullName) {
                    folderInbox.dropAllBackup(searchTerm)
                }
            }

            result
        }
    }

    private fun Store.getSentFolder(): Folder = getFolder(MailProperties.sentFolderNameByServer(urlName.username))

    fun getLastUIDSent(mailProp: MailProperties): Long? {

        val store = mailProp.connectImapStore()

        return store.use {
            val searchTerm = getSearchTerm(it.urlName.username)

            val folderSent = it.getSentFolder()

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

        return use { folder ->
            val messages = folder.search(searchTerm) ?: return ResponseFile.equalUidSuccess(lastUidSaved)

            val lastMessage = messages.getLastMessage()?: return ResponseFile.equalUidSuccess(lastUidSaved)

            val uid =  (folder as? UIDFolder)?.getUID(lastMessage)

            if(uid == lastUidSaved) return ResponseFile.equalUidSuccess(lastUidSaved)

            messages.dropMessagesWithout(lastMessage)

            lastMessage.getAttachUID(folder).apply {
                folder.expunge()
            }
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
            search(searchTerm)?.forEach { msg ->
                msg.setFlag(Flags.Flag.DELETED, true)
                //msg.saveChanges()
            }

            expunge()
        }
    }

    private fun Array<out Message>.dropMessagesWithout(without: Message?) {
        asSequence().filter { it != without }.forEach {
            it.setFlag(Flags.Flag.DELETED, true)
           // it.saveChanges()
        }
    }

    private fun Array<out Message>.getLastMessage(): Message? =
            asSequence().maxWithOrNull( compareBy { it.receivedDate?.time ?: Long.MIN_VALUE } )

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