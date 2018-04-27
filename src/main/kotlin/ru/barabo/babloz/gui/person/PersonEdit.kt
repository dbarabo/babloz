package ru.barabo.babloz.gui.person

import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Person
import ru.barabo.babloz.db.service.PersonService
import tornadofx.*

internal object PersonEdit : VBox() {
    init {
        form {
            fieldset {
                field("Наименование") {
                    textfield(property = PersonSaver.editBind.nameProperty)
                }
                field("Родительский проект") {
                    combobox<Person>(property = PersonSaver.editBind.parentProperty, values = PersonService.parentList())
                }
                field("Описание") {
                    textarea(property = PersonSaver.editBind.descriptionProperty).prefRowCount = 3
                }
            }
        }
    }
}