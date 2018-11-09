package ru.barabo.babloz.gui.budget

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import tornadofx.datepicker
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import java.time.LocalDate

object CreateBudgetMain : Dialog<Pair<LocalDate, LocalDate?>>() {

    private val dateStartProperty = SimpleObjectProperty<LocalDate>(LocalDate.now())

    init {
        title = "Создать новый Бюджет"

        headerText = "Выберите месяц нового бюджета"

        dialogPane.content =
                form {
                    fieldset {

                        field("Месяц бюджета") {
                            datepicker(property = dateStartProperty)
                        }
                    }
                }

        dialogPane.buttonTypes.setAll(ButtonType("Ok", ButtonBar.ButtonData.OK_DONE),
                ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE))

        this.setResultConverter {
            if (it.buttonData.isCancelButton) return@setResultConverter null

            return@setResultConverter Pair(dateStartProperty.value, null)
        }
    }
}