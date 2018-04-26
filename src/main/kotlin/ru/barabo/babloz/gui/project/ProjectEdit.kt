package ru.barabo.babloz.gui.project

import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.db.service.ProjectService
import tornadofx.*

internal object ProjectEdit : VBox() {
    init {
        form {
            fieldset {
                field("Наименование") {
                    textfield(property = ProjectSaver.editBind.nameProperty)
                }
                field("Родительский проект") {
                    combobox<Project>(property = ProjectSaver.editBind.parentProjectProperty, values = ProjectService.parentList())
                }
                field("Описание") {
                    textarea(property = ProjectSaver.editBind.descriptionProperty).prefRowCount = 3
                }
            }
        }
    }
}