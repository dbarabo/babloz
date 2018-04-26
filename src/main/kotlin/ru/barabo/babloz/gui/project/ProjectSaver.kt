package ru.barabo.babloz.gui.project

import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.db.service.ProjectService
import ru.barabo.babloz.gui.binding.AbstractSaver

object ProjectSaver : AbstractSaver<Project, ProjectBind>(ProjectBind::class.java) {

    override fun serviceSave(value: Project) {
        ProjectService.save(value)
    }
}