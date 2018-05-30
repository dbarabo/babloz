package ru.barabo.babloz.gui.budget

import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.layout.VBox
import org.controlsfx.control.CheckTreeView
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.service.budget.BudgetCategoryService
import tornadofx.*

internal object BudgetRowEdit : VBox() {
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
            CheckTreeView<GroupCategory>(CheckBoxTreeItem<GroupCategory>(BudgetCategoryService.elemRoot()) ).apply {

                populate { it.value.child }

                this.isShowRoot = false

                root.isExpanded = true
            }
}