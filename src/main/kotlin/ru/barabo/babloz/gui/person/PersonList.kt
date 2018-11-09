package ru.barabo.babloz.gui.person

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Person
import ru.barabo.babloz.db.entity.group.GroupPerson
import ru.barabo.babloz.db.service.PersonService
import ru.barabo.babloz.gui.account.addElemByLeft
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import tornadofx.*

object PersonList : Tab("Субъекты", VBox()), StoreListener<GroupPerson> {

    private var treeTable: TreeTableView<GroupPerson>? = null

    private var splitPane: SplitPane? = null

    init {
        form {
            toolbar {
                button ("Новый", ResourcesManager.icon("new.png")).apply {
                    setOnAction { showNewPerson() }
                }
                separator {  }

                button ("Сохранить", ResourcesManager.icon("save.png")).apply {
                    setOnAction { savePerson() }

                    disableProperty().bind(PersonSaver.isDisableEdit())
                }
                button ("Отменить", ResourcesManager.icon("cancel.png")).apply {
                    setOnAction { cancelPerson() }

                    disableProperty().bind(PersonSaver.isDisableEdit())
                }
            }
            splitpane(Orientation.HORIZONTAL, PersonEdit).apply { splitPane = this}
        }
        VBox.setVgrow(splitPane, Priority.ALWAYS)

        PersonService.addListener(this)
    }

    private fun showNewPerson() {

        PersonSaver.changeSelectEditValue(Person())
    }

    private fun savePerson() {

        PersonSaver.save()

        treeTable?.requestFocus()
    }

    private fun cancelPerson() {

        PersonSaver.cancel()

        treeTable?.requestFocus()
    }

    override fun refreshAll(elemRoot: GroupPerson, refreshType: EditType) {

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
                            PersonSaver.changeSelectEditValue(newSelection?.value?.person)
                        }

                splitPane?.addElemByLeft(treeTable!!, 0.45)
            }
        }
    }

    private fun treeTable(rootGroup: GroupPerson): TreeTableView<GroupPerson> {
        return TreeTableView<GroupPerson>().apply {
            column("Субъект/Группа", GroupPerson::name)

            column("Описание", GroupPerson::description)

            root = TreeItem(rootGroup)

            populate { it.value.child }

            root.isExpanded = true

            this.isShowRoot = false

            this.resizeColumnsToFitContent()
        }
    }
}