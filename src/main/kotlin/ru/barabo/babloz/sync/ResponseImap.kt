package ru.barabo.babloz.sync

import javax.mail.Store

data class ResponseImap(val isSuccess: Boolean = true, val imapConnect: Store? = null, val lastUidSaved: Long = 0) {
    companion object {
        val RESPONSE_IMAP_CANCEL: ResponseImap = ResponseImap(isSuccess = false)

        val RESPONSE_IMAP_LOCAL: ResponseImap = ResponseImap(isSuccess = true)
    }
}

