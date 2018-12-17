package ru.barabo.babloz.gui.report

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.ComboBox
import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import org.controlsfx.control.CheckTreeView
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.report.ChangeCategory
import ru.barabo.babloz.db.service.report.categoryturn.CategoryView
import ru.barabo.babloz.db.service.report.categoryturn.ReportServiceCategoryTurn
import ru.barabo.babloz.gui.pay.PayEdit.rowHeight
import ru.barabo.babloz.gui.pay.comboBoxDates
import ru.barabo.babloz.gui.pay.defaultRowCount
import tornadofx.*

object ReportCustomCategoryTurn : Tab("Динамика расходов по категориям", VBox()) {

    init {
        form {

            fieldset("Категории отчета") {
                addChildIfPossible(checkTreeCategory(ReportServiceCategoryTurn))
            }

            fieldset("Период отчета") {
                comboBoxDates(ReportServiceCategoryTurn::setDateRange)
            }

            fieldset("Рисовать категории") {
                comboBoxCustomCategory(ReportServiceCategoryTurn::setCategoryView)
            }
        }
    }
}

fun EventTarget.comboBoxCustomCategory(processCategoryView: (CategoryView)->Unit): ComboBox<CategoryView> {

    val dateSelectProperty = SimpleObjectProperty<CategoryView>(CategoryView.ALL)

    return combobox<CategoryView>(property = dateSelectProperty, values = CategoryView.values().toList()).apply {

        selectionModel?.selectedItemProperty()?.addListener { _, _, newSelection ->

            processCategoryView(newSelection)
        }
    }
}

internal fun checkTreeCategory(changeCategory: ChangeCategory) =
    CheckTreeView<GroupCategory>(CheckBoxTreeItem<GroupCategory>(CategoryService.categoryRoot()) ).apply {

        this.populate (itemFactory = {
            CheckBoxTreeItem(it, null, it.category.isSelected ?:0 != 0).apply {

                selectedProperty().addListener { _, _, newValue ->

                    if(newValue == true) {
                        changeCategory.addCategory(this.value.category)
                    } else {
                        changeCategory.removeCategory(this.value.category)
                    }
                }
            }
        },

        childFactory = { it.value.child })

        this.isShowRoot = false

        this.root.isExpanded = true

        prefHeight = rowHeight() * defaultRowCount(GroupCategory.countRoot()) * 2
    }
