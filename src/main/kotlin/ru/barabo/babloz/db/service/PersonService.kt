package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Person
import ru.barabo.babloz.db.entity.group.GroupPerson
import ru.barabo.db.service.StoreService

object PersonService : StoreService<Person, GroupPerson>(BablozOrm) {

    override fun clazz(): Class<Person> = Person::class.java

    public override fun elemRoot(): GroupPerson = GroupPerson.root

    override fun beforeRead() {
        GroupPerson.rootClear()
    }

    override fun processInsert(item: Person) {

        GroupPerson.addPerson(item)
    }

    fun parentList() :List<Person> {

        val list = ArrayList<Person>()

        list.add(NULL_PERSON)

        list.addAll( GroupPerson.root.child.map { it.person } )

        return list
    }

    private val NULL_PERSON = Person(name = "НЕТ")

    fun findPersonById(id :Int?) : Person {

        val groupPerson = id?.let{ GroupPerson.root.child.firstOrNull { it.person.id == id } }

        return groupPerson?.person ?: NULL_PERSON
    }
}