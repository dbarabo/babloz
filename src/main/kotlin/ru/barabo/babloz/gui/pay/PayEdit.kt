package ru.barabo.babloz.gui.pay

import com.sun.javafx.tk.Toolkit
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.VBox
import javafx.stage.Screen
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.entity.group.GroupPerson
import ru.barabo.babloz.db.entity.group.GroupProject
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.PersonService
import ru.barabo.babloz.db.service.ProjectService
import ru.barabo.babloz.gui.formatter.currencyTextFormatter
import tornadofx.*
import kotlin.math.max


internal object PayEdit: VBox() {

    private const val CATEGORY_ROW_COUNT = 9

     init {

        form {
            fieldset {

                 field("Счет") {
                    combobox<Account>(property = PaySaver.editBind.accountProperty, values = AccountService.accountList())
                 }

                 field("Дата") {
                     datepicker(property = PaySaver.editBind.dateProperty)
                 }

                 field("Категория") {

                     treeViewCategory()
                 }

                 field("Перевод на счет") {
                     combobox<Account>(property = PaySaver.editBind.accountTransferProperty,
                             values = AccountService.accountNullList()).apply {

                         selectionModel?.selectedItemProperty()?.addListener(
                                 { _, _, newSelection ->

                                     newSelection?.id?.apply { PaySaver.editBind.setSelectCategoryFromAccountTransfer() }
                                 })
                     }
                 }

                 field("Проект") {

                    treeViewProject()
                 }

                field("Субъект") {

                    treeViewPerson()
                }

                 field("Сумма") {
                     textfield().apply {
                         bind(PaySaver.editBind.amountProperty)

                         textFormatter = currencyTextFormatter()

                     }
                 }

                 field("Ремарка") {
                     textarea(PaySaver.editBind.descriptionProperty).apply {
                         prefRowCount = if(Screen.getPrimary().visualBounds.height /
                                 (Toolkit.getToolkit().fontLoader.getFontMetrics(this.label("").font).lineHeight.toDouble() + 7.0)
                         >= ALL_ROW_COUNT) 3 else 2

                         isWrapText = true
                     }
                 }
            }
        }
    }

    private fun Field.treeViewCategory(): TreeView<GroupCategory> {
        return treeview(TreeItem(CategoryService.categoryRoot())).apply {

            PaySaver.editBind.setTreeViewCategory(this)

            populate { it.value.child }

            root.isExpanded = true

            this.isShowRoot = false

            prefHeight = rowHeight() * CATEGORY_ROW_COUNT

            selectionModel?.selectedItemProperty()?.addListener(
                    { _, _, newSelection ->
                        PaySaver.editBind.setSelectCategoryFromTreeView(newSelection?.value)
                    })
        }
    }

    private fun Field.treeViewProject(): TreeView<GroupProject> {
        return treeview(TreeItem(ProjectService.elemRoot())).apply {

            PaySaver.editBind.setTreeViewProject(this)

            populate { it.value.child }

            this.isShowRoot = false

            root.isExpanded = true

            prefHeight = rowHeight() * projectRowCount()

            selectionModel?.selectedItemProperty()?.addListener(
                    { _, _, newSelection ->
                        PaySaver.editBind.setSelectProjectFromTreeView(newSelection?.value)
                    })
        }
    }

    private fun Field.treeViewPerson(): TreeView<GroupPerson> {
        return treeview(TreeItem(PersonService.elemRoot())).apply {

            PaySaver.editBind.setTreeViewPerson(this)

            populate { it.value.child }

            this.isShowRoot = false

            root.isExpanded = true

            prefHeight = rowHeight() * personRowCount()

            selectionModel?.selectedItemProperty()?.addListener(
                    { _, _, newSelection ->
                        PaySaver.editBind.setSelectPersonFromTreeView(newSelection?.value)
                    })
        }
    }

    private const val ALL_ROW_COUNT = 36

    private fun TreeView<*>.maxRowCount(): Int {
        val screenHeight = Screen.getPrimary().visualBounds.height

        return if(screenHeight / rowHeight() >= ALL_ROW_COUNT) 6 else 5
    }

    private fun TreeView<*>.personRowCount()= defaultRowCount(GroupPerson.countAll() )

    private fun TreeView<*>.projectRowCount()= defaultRowCount(GroupProject.countAll() )

    private fun TreeView<*>.defaultRowCount(realCount: Int) = if (realCount < maxRowCount() ) max(realCount, 3) else maxRowCount()

    private fun TreeView<*>.rowHeight() =
            Toolkit.getToolkit().fontLoader.getFontMetrics(this.label("").font).lineHeight.toDouble() + 7.0
}
