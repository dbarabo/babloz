package ru.barabo.babloz.db.entity.group

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Person
import tornadofx.observable

class GroupPerson(var person: Person = Person(),
                  private var parent : GroupPerson? = null,
                  val child: MutableList<GroupPerson> = ArrayList<GroupPerson>().observable() ) {
    companion object {
        private val logger = LoggerFactory.getLogger(GroupPerson::class.java)!!

        val root = GroupPerson()

        private var lastParent = root

        fun rootClear() {
            synchronized(root.child) { root.child.clear() }
        }

        fun countAll(): Int {

            val sum = root.child.map { it.child.size + 1}.sum()

            logger.info("sum=$sum")

            return sum
        }

        fun addPerson(person: Person): GroupPerson {

            val groupPerson = person.parent
                    ?.let { GroupPerson(person, lastParent) }
                    ?: GroupPerson(person, root).apply { lastParent = this }

            groupPerson.parent?.child?.add(groupPerson)

            logger.info("addPerson=$groupPerson")

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