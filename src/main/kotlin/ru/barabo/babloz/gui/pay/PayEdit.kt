package ru.barabo.babloz.gui.pay

import com.sun.javafx.tk.Toolkit
import javafx.scene.control.TreeItem
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.gui.formatter.currencyTextFormatter
import tornadofx.*


object PayEdit: VBox() {

    private const val ROW_COUNT = 9.0

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
                    var heightText: Double

                    treeview(TreeItem(CategoryService.categoryRoot())).apply {

                        heightText = Toolkit.getToolkit().fontLoader.getFontMetrics(this.label("").font).lineHeight.toDouble() + 7.0

                        PaySaver.editBind.setTreeViewCategory(this)

                        populate { it.value.child }

                        root.isExpanded = true

                        this.isShowRoot = false

                        prefHeight = heightText * ROW_COUNT

                        selectionModel?.selectedItemProperty()?.addListener(
                                { _, _, newSelection ->
                                    PaySaver.editBind.setSelectCategoryFromTreeView(newSelection?.value)
                                })
                    }
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

                field("Сумма") {
                    textfield().apply {
                        bind(PaySaver.editBind.amountProperty)

                        textFormatter = currencyTextFormatter()

                    }
                }

                field("Ремарка") {
                    textarea(PaySaver.editBind.descriptionProperty).prefRowCount = 3
                }
            }
        }
    }
}
