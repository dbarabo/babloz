package ru.barabo.babloz.gui.category

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.Category.Companion.setDatePeriod
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.gui.account.addElemByLeft
import ru.barabo.babloz.gui.pay.comboBoxDates
import ru.barabo.babloz.gui.pay.gotoPayListByDateCategory
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import tornadofx.*

object CategoryList : Tab("Категории", VBox()), StoreListener<GroupCategory> {

    private var treeTable: TreeTableView<GroupCategory>? = null

    private var splitPane: SplitPane? = null

    init {
        form {
            toolbar {
                button ("Новый", ResourcesManager.icon("new.png")).apply {
                    setOnAction { showNewCategory() }
                }
                separator {  }

                button ("Сохранить", ResourcesManager.icon("save.png")).apply {
                    setOnAction { saveCategory() }

                    disableProperty().bind(CategorySaver.isDisableEdit())
                }
                button ("Отменить", ResourcesManager.icon("cancel.png")).apply {
                    setOnAction { cancelCategory() }

                    disableProperty().bind(CategorySaver.isDisableEdit())
                }

                button ("В платежи->", ResourcesManager.icon("gopay.png")).apply {
                    setOnAction { goToPay() }
                }

                comboBoxDates(::setDatePeriod)
            }
            splitpane(Orientation.HORIZONTAL, CategoryEdit).apply { splitPane = this}
        }
        VBox.setVgrow(splitPane, Priority.ALWAYS)

        CategoryService.addListener(this)
    }

    private fun goToPay() {

        val selectItem = treeTable?.selectedItem?:return

        val items = ArrayList(selectItem.child.map { it.category })

        items += selectItem.category

        val (start, end) = Category.getDatePeriod()

        gotoPayListByDateCategory(start, end, items)
    }

    private fun showNewCategory() {

        CategorySaver.changeSelectEditValue(Category())
    }

    private fun saveCategory() {

        CategorySaver.save()

        treeTable?.requestFocus()
    }

    private fun cancelCategory() {

        CategorySaver.cancel()

        treeTable?.requestFocus()
    }

    override fun refreshAll(elemRoot: GroupCategory, refreshType: EditType) {

        treeTable?.let {
            Platform.runLater({ run {it.refresh()} })
            return
        }

        Platform.runLater({
            run {
                synchronized(elemRoot) {
                    treeTable = treeTable(elemRoot)
                }

                treeTable?.selectionModel?.selectedItemProperty()?.addListener(
                        { _, _, newSelection ->
                            CategorySaver.changeSelectEditValue(newSelection?.value?.category)
                        })

                splitPane?.addElemByLeft(treeTable!!, 0.45)
            }
        })
    }

    private fun treeTable(rootGroup: GroupCategory): TreeTableView<GroupCategory> {
        return TreeTableView<GroupCategory>().apply {
            column("Категория", GroupCategory::name)

            column("Тип", GroupCategory::type)

            column("Сумма платежей", GroupCategory::turn)

            root = TreeItem(rootGroup)

            populate { it.value.child }

            root.isExpanded = true

            this.isShowRoot = false

            this.resizeColumnsToFitContent()
        }
    }
}