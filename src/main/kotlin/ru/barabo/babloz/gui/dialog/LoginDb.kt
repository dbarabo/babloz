package ru.barabo.babloz.gui.dialog

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import ru.barabo.babloz.db.entity.Profile
import ru.barabo.babloz.db.service.ProfileService
import ru.barabo.babloz.sync.ResponseImap
import ru.barabo.babloz.sync.SyncTypes
import ru.barabo.babloz.sync.SyncZip
import ru.barabo.crypto.CheckerHash
import tornadofx.*

object LoginDb : Dialog<ResponseImap>() {

   // private val logger = LoggerFactory.getLogger(LoginDb::class.java)!!

    private val mailAccount = SimpleStringProperty("")

    private const val DEFAULT_MAIL = "@gmail.com"

    private val passwordAccount = SimpleStringProperty("")

    private val syncType = SimpleObjectProperty<SyncTypes>(SyncTypes.SYNC_START_SAVE_LOCAL)

    private var profile = ProfileService.dataProfile()

    init {
        title = "Синхронизация с gmail-аккаунтом"

        headerText = "Укажите почтовый email-ящик, пароль к нему для синхронизации базы"

        fromProfile()

        dialogPane.content =
                form {
                    fieldset {

                        field("gmail-аккаунт вида (xyz@gmail.com)") {
                            textfield(property = mailAccount)
                        }

                        field("Пароль к gmail-аккаунту") {
                            passwordfield(property = passwordAccount)
                        }

                        field("Тип синхронизации") {
                            combobox<SyncTypes>(property = syncType, values = SyncTypes.values().toList())
                        }
                    }
                }

        dialogPane.buttonTypes.setAll(ButtonType("Ok", ButtonBar.ButtonData.OK_DONE),
                ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE))

        this.setResultConverter { processResult(it.buttonData.isCancelButton) }
    }

    fun startSyncDialog(processResponse: (ResponseImap)->Unit) {
        val profile = ProfileService.dataProfile()

        val response = showWait(profile)

        processResponse(response)

        if(response.isSuccess) {
            profile.msgUidSync = SyncZip.startSync(response)

            ProfileService.save(profile)
        }
    }

    fun sendSyncBackupDialog() {
        val profile = ProfileService.dataProfile()

        val response = showWait(profile)

        response.imapConnect?.close()

        if(response.isSuccess) {
            SyncZip.saveEndBackup()
        }
    }

    private fun showWait(profile: Profile): ResponseImap {
        this.profile = profile

        fromProfile()

        return showAndWait().orElse(null)
    }

    private fun processResult(isCancelButton: Boolean): ResponseImap {
        if(isCancelButton) return ResponseImap.RESPONSE_IMAP_CANCEL

        val responseImap = getImapConnect()
        if(!responseImap.isSuccess) {
            return showAndWait().orElseGet { ResponseImap.RESPONSE_IMAP_CANCEL }
        }

        val hashPassword = passwordAccount.value?.let { CheckerHash.toHashPassword(it) }

        if(responseImap.imapConnect == null &&
            isNotCheckPassword(hashPassword) ) {
                alert(Alert.AlertType.ERROR, "Неверно указан пароль к локальной базе")

                return showAndWait().orElseGet { ResponseImap.RESPONSE_IMAP_CANCEL }
        }

        return saveProfile(responseImap, hashPassword)
    }

    private fun getImapConnect(): ResponseImap {
        if(syncType.value == SyncTypes.NO_SYNC_LOCAL_ONLY) return ResponseImap.RESPONSE_IMAP_LOCAL

        if(mailAccount.value.isNullOrEmpty() || passwordAccount.value.isNullOrEmpty() ) return ResponseImap.RESPONSE_IMAP_CANCEL

        return SyncZip.getImapConnect(mailAccount.value, passwordAccount.value, syncType.value, profile.msgUidSync?:0L)
    }

    private fun saveProfile(responseImap: ResponseImap, hashPassword: String?): ResponseImap {

        if(!responseImap.isSuccess) return responseImap

        profile.mail = mailAccount.value

        profile.syncType = syncType.value?:SyncTypes.NO_SYNC_LOCAL_ONLY

        profile.pswdHash = hashPassword

        ProfileService.save(profile)

        return responseImap
    }

    private fun isNotCheckPassword(newPasswordHash: String?): Boolean =
                if(profile.pswdHash?:"" == "") false else profile.pswdHash != newPasswordHash


    private fun fromProfile() {
        mailAccount.value = profile.mail ?: DEFAULT_MAIL

        passwordAccount.value = ""

        syncType.value = profile.syncType ?: SyncTypes.NO_SYNC_LOCAL_ONLY
    }
}