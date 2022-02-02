package ru.barabo.babloz.db.entity.group

import ru.barabo.babloz.db.entity.Person
import ru.barabo.babloz.gui.formatter.toCurrencyFormat
import tornadofx.asObservable

class GroupPerson(var person: Person = Person(),
                  private var parent : GroupPerson? = null,
                  val child: MutableList<GroupPerson> = ArrayList<GroupPerson>().asObservable()
) {

    companion object {
        //private val logger = LoggerFactory.getLogger(GroupPerson::class.java)!!

        val NULL_PERSON = Person(name = "НЕТ")

        val root = GroupPerson()

        private val NULL_GROUP_PERSON = GroupPerson(NULL_PERSON, root).apply { root.child.add(this) }

        private var lastParent = root

        fun rootClear() {
            synchronized(root.child) {
                root.child.clear()

                root.child.add(NULL_GROUP_PERSON)
            }
        }

        fun addPerson(person: Person): GroupPerson {

            val groupPerson = person.parent
                    ?.let { GroupPerson(person, lastParent) }
                    ?: GroupPerson(person, root).apply { lastParent = this }

            groupPerson.parent?.child?.add(groupPerson)

            return groupPerson
        }

        fun findByPerson(person: Person): GroupPerson? {

            return root.findByPerson(person)
        }
    }

    val name: String get() = person.name ?: ""

    val description: String get() = person.description ?: ""

    val debt: String get() = person.debt.toCurrencyFormat()

    val credit: String get() = person.credit.toCurrencyFormat()

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