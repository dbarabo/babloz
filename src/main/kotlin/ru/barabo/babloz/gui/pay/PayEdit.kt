package ru.barabo.babloz.gui.pay

import com.sun.javafx.tk.Toolkit
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Screen
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.entity.group.GroupPerson
import ru.barabo.babloz.db.entity.group.GroupProject
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.PersonService
import ru.barabo.babloz.db.service.ProjectService
import ru.barabo.babloz.gui.budget.BudgetRowEdit
import ru.barabo.babloz.gui.formatter.currencyTextFormatter
import tornadofx.*
import kotlin.math.max


internal object PayEdit: VBox() {

    private val logger = LoggerFactory.getLogger(PayEdit::class.java)

    private const val CATEGORY_ROW_COUNT = 9

    private lateinit var infoNewField: Text

     init {

        form {
            fieldset {

                text(property = PaySaver.editBind.newPayProperty).apply { infoNewField = this }

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
                         prefRowCount = 2/*if(Screen.getPrimary().visualBounds.height /
                                 (Toolkit.getToolkit().fontLoader.getFontMetrics(this.label("").font).lineHeight.toDouble()*1.5)
                         >= ALL_ROW_COUNT) 3 else 2*/

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

            prefHeight = rowHeight() * calcHeight() //CATEGORY_ROW_COUNT

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

    private const val ALL_ROW_COUNT = 26

    private var countProject: Int = 0

    private fun TreeView<*>.calcHeight(): Int {
        val screenHeight = Screen.getPrimary().visualBounds.height

        //logger.error("screenHeight=$screenHeight")
        //logger.error("rowHeight=${rowHeight()}")

        var maxCountProject = 6
        var allCount = 36
        while ((screenHeight / rowHeight() < allCount) && maxCountProject > 3) {
            maxCountProject--
            allCount -= 2
        }

        var maxCategoryCount = CATEGORY_ROW_COUNT
        while(screenHeight / rowHeight() < allCount) {
            allCount--
            maxCategoryCount--
        }

        countProject = maxCountProject

        return maxCategoryCount
    }

    private fun TreeView<*>.personRowCount()= countProject //defaultRowCount(GroupPerson.countAll() )

    private fun TreeView<*>.projectRowCount()= countProject //defaultRowCount(GroupProject.countAll() )

    //fun TreeView<*>.defaultRowCount(realCount: Int) = if (realCount < maxRowCount() ) max(realCount, 3) else maxRowCount()

    fun TreeView<*>.rowHeight() =
            Toolkit.getToolkit().fontLoader.getFontMetrics(this.label("").font).lineHeight.toDouble()* 1.49
}
