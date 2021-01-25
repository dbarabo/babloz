package ru.barabo.babloz.db.entity.group

import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.gui.formatter.toCurrencyFormat
import tornadofx.observable
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

data class GroupProject(var project: Project = Project(),
                        private var parent : GroupProject? = null,
                        val child: MutableList<GroupProject> = ArrayList<GroupProject>().observable() ) {
    companion object {
        val root = GroupProject()

        private var lastParent = root

        fun rootClear() {
            synchronized(root.child) { root.child.clear() }
        }

        fun addProject(project: Project): GroupProject {

            val groupProject = project.parent
                    ?.let { GroupProject(project, lastParent) }
                    ?: GroupProject(project, root).apply { lastParent = this }

            groupProject.parent?.child?.add(groupProject)

            return groupProject
        }

        fun findByProject(project: Project): GroupProject? {

            return root.findByProject(project)
        }

        fun findByDescription(projectDesc: String): GroupProject? =
                root.findByDescription(projectDesc)
    }

    val name: String get() = project.name ?: ""

    val description: String get() = project.description ?: ""

    val turn: String get() = project.turn?.toCurrencyFormat() ?: ""

    val start: String get() = project.startProject?.toFormat() ?: ""

    val end: String get() = project.endProject?.toFormat() ?: ""

    val duration: String get() = if(project.startProject == null || project.endProject == null) ""
        else {
            val period = Period.between(project.startProject, project.endProject)

          "${period.years.ifNotZero("г")} ${period.months.ifNotZero("м")} ${period.days.ifNotZero("д")}".trim()
    }
        override fun toString(): String = name

    private fun findByProject(findProject: Project): GroupProject? {
        if(findProject.id == project.id) return this

        for (group in child) {
            val find = group.findByProject(findProject)

            if(find != null) {
                return find
            }
        }
        return null
    }

    private fun findByDescription(projectDescUpper: String): GroupProject? {
        if(projectDescUpper.equals(project.description, true) ) return this

        for (group in child) {
            val find = group.findByDescription(projectDescUpper)

            if(find != null) {
                return find
            }
        }
        return null
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun LocalDate.toFormat(): String = dateFormatter.format(this)

private fun Int.ifNotZero(postfix: String): String = if(this == 0)"" else "${this}${postfix}";