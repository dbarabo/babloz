package ru.barabo.babloz.gui.project

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.db.entity.group.GroupProject
import ru.barabo.babloz.db.service.ProjectService
import ru.barabo.babloz.gui.account.addElemByLeft
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import tornadofx.*

object ProjectList : Tab("Проекты", VBox()), StoreListener<GroupProject> {

    private var treeTable: TreeTableView<GroupProject>? = null

    private var splitPane: SplitPane? = null

    init {
        form {
            toolbar {
                button ("Новый", ResourcesManager.icon("new.png")).apply {
                    setOnAction { showNewProject() }
                }
                separator {  }

                button ("Сохранить", ResourcesManager.icon("save.png")).apply {
                    setOnAction { saveProject() }

                    disableProperty().bind(ProjectSaver.isDisableEdit())
                }
                button ("Отменить", ResourcesManager.icon("cancel.png")).apply {
                    setOnAction { cancelProject() }

                    disableProperty().bind(ProjectSaver.isDisableEdit())
                }
            }
            splitpane(Orientation.HORIZONTAL, ProjectEdit).apply { splitPane = this}
        }
        VBox.setVgrow(splitPane, Priority.ALWAYS)

        ProjectService.addListener(this)
    }

    private fun showNewProject() {

        ProjectSaver.changeSelectEditValue(Project())
    }

    private fun saveProject() {

        ProjectSaver.save()

        treeTable?.requestFocus()
    }

    private fun cancelProject() {

        ProjectSaver.cancel()

        treeTable?.requestFocus()
    }

    override fun refreshAll(elemRoot: GroupProject, refreshType: EditType) {

        treeTable?.let {
            Platform.runLater { run {it.refresh()} }
            return
        }

        Platform.runLater {
            run {
                synchronized(elemRoot) {
                    treeTable = treeTable(elemRoot)
                }

                treeTable?.selectionModel?.selectedItemProperty()?.addListener { _, _, newSelection ->
                            ProjectSaver.changeSelectEditValue(newSelection?.value?.project)
                        }

                splitPane?.addElemByLeft(treeTable!!, 0.45)
            }
        }
    }

    private fun treeTable(rootGroup: GroupProject): TreeTableView<GroupProject> {
        return TreeTableView<GroupProject>().apply {
            column("Проект", GroupProject::name)

            column("Описание", GroupProject::description)

            root = TreeItem(rootGroup)

            populate { it.value.child }

            root.isExpanded = true

            this.isShowRoot = false

            this.resizeColumnsToFitContent()
        }
    }
}