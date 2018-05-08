package ru.barabo.babloz.gui.dialog

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import ru.barabo.babloz.sync.SyncTypes
import tornadofx.*

object LoginDb : Dialog<Triple<String?, String?, SyncTypes>>() {

    private val mailAccount = SimpleStringProperty("@gmail.com")

    private val passwordAccount = SimpleStringProperty("")

    private val syncType = SimpleObjectProperty<SyncTypes>(SyncTypes.values()[0])

    init {
        title = "Синхронизация с gmail-аккаунтом"

        headerText = "Укажите почтовый email-ящик, пароль к нему для синхронизации базы"

        dialogPane.content =
                form {
                    fieldset {

                        field("gmail-аккаунт вида (xyz@gmail.com)") {
                            textfield(property = mailAccount)
                        }

                        field("Пароль к gmail-аккаунту") {
                            textfield(property = passwordAccount)
                        }

                        combobox<SyncTypes>(property = syncType, values = SyncTypes.values().toList())
                    }
                }

        dialogPane.buttonTypes.setAll(ButtonType("Ok", ButtonBar.ButtonData.OK_DONE),
                ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE))

        this.setResultConverter({
            if (it.buttonData.isCancelButton) return@setResultConverter null

            return@setResultConverter Triple(mailAccount.value, passwordAccount.value, syncType.value)
        })
    }
}