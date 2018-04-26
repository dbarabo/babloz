package ru.barabo.babloz.gui.project

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.db.service.ProjectService
import ru.barabo.babloz.gui.binding.BindProperties

class ProjectBind : BindProperties<Project> {

    val nameProperty = SimpleStringProperty()

    val parentProjectProperty = SimpleObjectProperty<Project>()

    val descriptionProperty = SimpleStringProperty()

    override var editValue: Project? = null

    override fun fromValue(value: Project?) {
        nameProperty.value = value?.name

        parentProjectProperty.value = ProjectService.findProjectById(value?.parent)

        descriptionProperty.value = value?.description
    }

    override fun toValue(value: Project) {
        value.name = nameProperty.value

        value.description = descriptionProperty.value

        value.parent = parentProjectProperty.value?.id
    }

    override fun copyToProperties(destination: BindProperties<Project>) {
        val destinationProject = destination as ProjectBind

        destinationProject.nameProperty.value = nameProperty.value

        destinationProject.parentProjectProperty.value = parentProjectProperty.value

        destinationProject.descriptionProperty.value = descriptionProperty.value
    }

    override fun isEqualsProperties(compare: BindProperties<Project>): BooleanBinding {

        val compareProject = compare as ProjectBind

        return Bindings.and(
                nameProperty.isEqualTo(compareProject.nameProperty),
                parentProjectProperty.isEqualTo(compareProject.parentProjectProperty)
                        .and(descriptionProperty.isEqualTo(compareProject.descriptionProperty)) )
    }
}