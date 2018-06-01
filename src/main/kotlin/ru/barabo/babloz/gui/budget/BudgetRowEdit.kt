package ru.barabo.babloz.gui.budget

import javafx.collections.ListChangeListener
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.TreeItem
import javafx.scene.layout.VBox
import org.controlsfx.control.CheckTreeView
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.service.budget.BudgetTreeCategoryService
import tornadofx.*

internal object BudgetRowEdit : VBox() {

    private val logger = LoggerFactory.getLogger(BudgetRowEdit::class.java)

    init {
        form {
            fieldset {
                field("Наименование") {
                    textfield()
                }
                field("Выделенный лимит") {
                    textfield()
                }
                field("Категории строки бюджета") {
                    addChildIfPossible( checkTreeBudgetCategory() )
                }
            }
        }
    }

    private fun Field.checkTreeBudgetCategory(): CheckTreeView<GroupCategory> =
            CheckTreeView<GroupCategory>(CheckBoxTreeItem<GroupCategory>(BudgetTreeCategoryService.elemRoot()) ).apply {

                populate (itemFactory = {CheckBoxTreeItem(it, null, it.category.isSelected?.toInt()?:0 != 0)},  childFactory = { it.value.child })

                this.isShowRoot = false

                root.isExpanded = true

                checkModel.checkedItems.addListener { change: ListChangeListener.Change<out TreeItem<GroupCategory>> ->

                    while (change.next()) {
                        if(change.wasAdded()) {
                            change.addedSubList.forEach { BudgetTreeCategoryService.addCategory(it.value) }
                        }

                        if(change.wasRemoved()) {
                            change.removed.forEach { BudgetTreeCategoryService.removeCategory(it.value) }
                        }
                    }
                 }
            }
}