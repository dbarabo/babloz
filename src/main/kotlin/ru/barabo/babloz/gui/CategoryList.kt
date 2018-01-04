package ru.barabo.babloz.gui

import javafx.application.Platform
import javafx.scene.control.Tab
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.GroupCategory
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.service.StoreListener
import tornadofx.*

object CategoryList : Tab("Категории", VBox()), StoreListener<GroupCategory> {
    private var forma: Form? = null

    private var treeTable: TreeTableView<GroupCategory>? = null

    private var selectGroupCategory: GroupCategory? = null

    init {
        this.graphic = ResourcesManager.icon("tree.png")

        forma = form {
            toolbar {
                button ("Сохранить", ResourcesManager.icon("save.png")).setOnAction { save() }

                button ("Отменить", ResourcesManager.icon("cancel.png")).setOnAction { cancel() }
            }
        }
        CategoryService.addListener(this)
    }

    private fun save() {
        tabPane.tabs.remove(CategoryList)
    }

    private fun cancel() {
        tabPane.tabs.remove(CategoryList)
    }

    override fun refreshAll(elemRoot: GroupCategory) {

        Platform.runLater({
            run {
                treeTable?.removeFromParent()

                synchronized(elemRoot) {
                    treeTable = treeTable(elemRoot)
                }

                treeTable?.root?.children?.forEach { it.expandedProperty().set(true) }

                treeTable?.selectionModel?.selectedItemProperty()?.addListener(
                        { _, _, newSelection ->
                            selectGroupCategory = newSelection?.value
                        })

                forma?.addChildIfPossible(treeTable!!)

                VBox.setVgrow(treeTable, Priority.ALWAYS)
            }
        })
    }

    private fun treeTable(rootGroup: GroupCategory): TreeTableView<GroupCategory> {
        return TreeTableView<GroupCategory>().apply {
            column("Категория", GroupCategory::name)

            column("Тип", GroupCategory::type)

            root = TreeItem(rootGroup)

            populate { it.value.child }

            root.isExpanded = true

            this.isShowRoot = false

            this.resizeColumnsToFitContent()
        }
    }
}