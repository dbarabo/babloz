package ru.barabo.babloz.gui.budget

import javafx.scene.control.Alert
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.layout.VBox
import org.controlsfx.control.CheckTreeView
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.service.budget.BudgetTreeCategoryService
import ru.barabo.babloz.gui.pay.PayEdit.defaultRowCount
import ru.barabo.babloz.gui.pay.PayEdit.rowHeight
import tornadofx.*

internal object BudgetRowEdit : VBox() {

    private val logger = LoggerFactory.getLogger(BudgetRowEdit::class.java)

    init {
        form {
            fieldset {
                field("Наименование") {
                    textfield(property = BudgetRowSaver.editBind.nameProperty).apply { isDisable = true }
                }
                field("Выделенный лимит") {
                    textfield(property = BudgetRowSaver.editBind.amountProperty)
                }
                field("Категории строки бюджета") {
                    addChildIfPossible( checkTreeBudgetCategory() )
                }
            }
        }
    }

    private fun checkTreeBudgetCategory(): CheckTreeView<GroupCategory> =
            CheckTreeView<GroupCategory>(CheckBoxTreeItem<GroupCategory>(BudgetTreeCategoryService.elemRoot()) ).apply {

                this.populate (itemFactory = {
                    CheckBoxTreeItem(it, null, it.category.isSelected?.toInt()?:0 != 0).apply {

                        selectedProperty().addListener { _, _, newValue ->

                            if(BudgetRow.budgetRowSelected?.isOther() == true) {

                                alert(Alert.AlertType.ERROR, ERROR_CHANGE_OTHER_ROW)
                                return@addListener
                            }

                            if(newValue == true) {
                                BudgetTreeCategoryService.addCategory(this.value)
                            } else {
                                BudgetTreeCategoryService.removeCategory(this.value)
                            }
                            //logger.error("oldValue=$oldValue newValue=$newValue item = $this")
                        }
                    }
                },

                 childFactory = { it.value.child })

                this.isShowRoot = false

                this.root.isExpanded = true

                prefHeight = rowHeight() * defaultRowCount(GroupCategory.countRoot())
            }

    private const val ERROR_CHANGE_OTHER_ROW = "Нельзя менять список у строки <Все остальные категории>"
}