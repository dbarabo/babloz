package ru.barabo.babloz.gui.person

import ru.barabo.babloz.db.entity.Person
import ru.barabo.babloz.db.service.PersonService
import ru.barabo.babloz.gui.binding.AbstractSaver

object PersonSaver : AbstractSaver<Person, PersonBind>(PersonBind::class.java) {

    override fun serviceSave(value: Person) {
        PersonService.save(value)
    }
}