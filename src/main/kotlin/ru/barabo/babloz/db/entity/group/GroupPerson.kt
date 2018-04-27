package ru.barabo.babloz.db.entity.group

import ru.barabo.babloz.db.entity.Person
import tornadofx.observable

class GroupPerson(var person: Person = Person(),
                  private var parent : GroupPerson? = null,
                  val child: MutableList<GroupPerson> = ArrayList<GroupPerson>().observable() ) {
    companion object {
        val root = GroupPerson()

        private var lastParent = root

        fun rootClear() {
            synchronized(root.child) { root.child.clear() }
        }

        fun countAll() = root.child.map { it.child.size }.sum()

        fun addPerson(person: Person): GroupPerson {

            val groupPerson = person.parent
                    ?.let { GroupPerson(person, lastParent) }
                    ?: GroupPerson(person, root).apply { lastParent = this }

            groupPerson.parent?.child?.add(groupPerson)

            return groupPerson
        }

        fun findByPerson(person: Person): GroupPerson? {

            return GroupPerson.root.findByPerson(person)
        }
    }

    val name: String get() = person.name?.let { it } ?: ""

    val description: String get() = person.description?.let{ it } ?: ""

    override fun toString(): String = name

    private fun findByPerson(findPerson: Person): GroupPerson? {
        if(findPerson.id == person.id) return this

        for (group in child) {
            val find = group.findByPerson(findPerson)

            if(find != null) {
                return find
            }
        }
        return null
    }
}