package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.entity.Person
import ru.barabo.babloz.db.entity.group.GroupAccount
import ru.barabo.babloz.db.entity.group.GroupPerson
import ru.barabo.babloz.db.entity.group.GroupPerson.Companion.NULL_PERSON
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.db.service.StoreService

private class delegateUpdater<T> : StoreListener<T> {

    override fun refreshAll(elemRoot: T, refreshType: EditType) {
        if(refreshType.isEditable()) {
            PersonService.initData()
        }
    }
}

object PersonService : StoreService<Person, GroupPerson>(BablozOrm, Person::class.java){

    init {
        PayService.addListener(delegateUpdater<List<Pay>>() )

        AccountService.addListener(delegateUpdater<GroupAccount>() )
    }

    public override fun elemRoot(): GroupPerson = GroupPerson.root

    override fun beforeRead() {
        GroupPerson.rootClear()
    }

    override fun processInsert(item: Person) {
        GroupPerson.addPerson(item)
    }

    fun parentList() :List<Person> = GroupPerson.root.child.map { it.person }
//
//    {
//
//        val list = ArrayList<Person>()
//
//        list.add(NULL_PERSON)
//
//        list.addAll( GroupPerson.root.child.map { it.person } )
//
//        return list
//    }

    fun findPersonById(id :Int?) : Person {

        val groupPerson = id?.let{idIt -> GroupPerson.root.child.firstOrNull { it.person.id == idIt } }

        return groupPerson?.person ?: NULL_PERSON
    }
}