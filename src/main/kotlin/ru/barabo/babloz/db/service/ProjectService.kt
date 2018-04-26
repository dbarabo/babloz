package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.db.entity.group.GroupProject
import ru.barabo.db.service.StoreService

object ProjectService : StoreService<Project, GroupProject>(BablozOrm) {

    override fun clazz(): Class<Project> = Project::class.java

    override fun elemRoot(): GroupProject = GroupProject.root

    override fun beforeRead() {
        GroupProject.rootClear()
    }

    override fun processInsert(item: Project) {

        GroupProject.addProject(item)
    }

    fun parentList() :List<Project> {

        val list = ArrayList<Project>()

        list.add(NULL_PROJECT)

        list.addAll( GroupProject.root.child.map { it.project } )

        return list
    }

    private val NULL_PROJECT = Project(name = "НЕТ")

    fun findProjectById(id :Int?) : Project {

        val groupProject = id?.let{ GroupProject.root.child.firstOrNull { it.project.id == id } }

        return groupProject?.project ?: NULL_PROJECT
    }
}