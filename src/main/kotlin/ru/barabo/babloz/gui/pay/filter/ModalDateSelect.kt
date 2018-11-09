package ru.barabo.babloz.gui.pay.filter

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import tornadofx.datepicker
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import java.time.LocalDate

object ModalDateSelect : Dialog<Pair<LocalDate, LocalDate>>() {

    private val start = SimpleObjectProperty<LocalDate>(LocalDate.now())

    private val end = SimpleObjectProperty<LocalDate>(LocalDate.now())

    init {
        title = "Задать период"

        headerText = "Выберите даты начала и окончания периода"

        dialogPane.content =
                form {
                    fieldset {

                        field("Начало") {
                            datepicker(property = start)
                        }

                        field("Окончание") {
                            datepicker(property = end)
                        }
                    }
                }

        dialogPane.buttonTypes.setAll(ButtonType("Задать", ButtonBar.ButtonData.OK_DONE),
                ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE))

        this.setResultConverter{
            if (it.buttonData.isCancelButton) return@setResultConverter null

            return@setResultConverter Pair(start.value, end.value)
        }
    }
}