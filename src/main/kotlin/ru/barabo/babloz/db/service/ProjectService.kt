package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.db.entity.group.GroupProject
import ru.barabo.db.service.StoreService

object ProjectService : StoreService<Project, GroupProject>(BablozOrm, Project::class.java) {

    public override fun elemRoot(): GroupProject = GroupProject.root

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

    val ALL_PROJECT = Project(name = "ВСЕ Проекты")

    fun findProjectById(id :Int?) : Project {

        val groupProject = id?.let {idIt -> GroupProject.root.child.firstOrNull { it.project.id == idIt } }

        return groupProject?.project ?: NULL_PROJECT
    }

    fun projectAllList(): List<Project> {
        val result = ArrayList<Project>()

        result += ALL_PROJECT

        result.addAll(dataList)

        return result
    }

    fun projectList() = dataList
}