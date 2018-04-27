package ru.barabo.babloz.gui.person

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import ru.barabo.babloz.db.entity.Person
import ru.barabo.babloz.db.service.PersonService
import ru.barabo.babloz.gui.binding.BindProperties

class PersonBind : BindProperties<Person> {

    val nameProperty = SimpleStringProperty()

    val parentProperty = SimpleObjectProperty<Person>()

    val descriptionProperty = SimpleStringProperty()

    override var editValue: Person? = null

    override fun fromValue(value: Person?) {
        nameProperty.value = value?.name

        parentProperty.value = PersonService.findPersonById(value?.parent)

        descriptionProperty.value = value?.description
    }

    override fun toValue(value: Person) {
        value.name = nameProperty.value

        value.description = descriptionProperty.value

        value.parent = parentProperty.value?.id
    }

    override fun copyToProperties(destination: BindProperties<Person>) {
        val destinationPerson = destination as PersonBind

        destinationPerson.nameProperty.value = nameProperty.value

        destinationPerson.parentProperty.value = parentProperty.value

        destinationPerson.descriptionProperty.value = descriptionProperty.value
    }

    override fun isEqualsProperties(compare: BindProperties<Person>): BooleanBinding {

        val comparePerson = compare as PersonBind

        return Bindings.and(
                nameProperty.isEqualTo(comparePerson.nameProperty),
                parentProperty.isEqualTo(comparePerson.parentProperty)
                        .and(descriptionProperty.isEqualTo(comparePerson.descriptionProperty)) )
    }
}