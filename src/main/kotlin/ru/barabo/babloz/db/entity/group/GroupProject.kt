package ru.barabo.babloz.db.entity.group

import ru.barabo.babloz.db.entity.Project
import tornadofx.observable

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
    }

    val name: String get() = project.name?.let { it } ?: ""

    val description: String get() = project.description?.let{ it } ?: ""

    override fun toString(): String = name
}