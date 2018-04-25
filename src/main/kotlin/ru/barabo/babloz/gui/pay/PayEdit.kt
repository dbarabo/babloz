package ru.barabo.babloz.gui.pay

import com.sun.javafx.tk.Toolkit
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.TreeItem
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.gui.custom.ChangeSelectEdit
import ru.barabo.babloz.gui.formatter.currencyTextFormatter
import tornadofx.*


object PayEdit: VBox() { //Tab("Правка платежа", VBox()) {

    private val logger = LoggerFactory.getLogger(PayEdit::class.java)

    private const val ROW_COUNT = 9.0

    private var changeSelectEdit = ChangeSelectEdit.SAVE

    private val payEditBind = PayBind()

    private val oldSavePayBind = PayBind()

    fun isDisableEdit() = payEditBind.isDisableEdit(oldSavePayBind)

     init {

        form {

         fieldset {

                field("Счет") {
                    combobox<Account>(property = payEditBind.accountProperty, values = AccountService.accountList())
                }

                field("Дата") {
                    datepicker(property = payEditBind.dateProperty)
                }

                field("Категория") {
                    var heightText: Double

                    treeview(TreeItem(CategoryService.categoryRoot())).apply {

                        heightText = Toolkit.getToolkit().fontLoader.getFontMetrics(this.label("").font).lineHeight.toDouble() + 7.0

                        payEditBind.setTreeViewCategory(this)

                        populate { it.value.child }

                        root.isExpanded = true

                        this.isShowRoot = false

                        prefHeight = heightText * ROW_COUNT

                        selectionModel?.selectedItemProperty()?.addListener(
                                { _, _, newSelection ->
                                    payEditBind.setSelectCategoryFromTreeView(newSelection?.value)
                                })
                    }
                }

                field("Перевод на счет") {
                    combobox<Account>(property = payEditBind.accountTransferProperty,
                            values = AccountService.accountNullList()).apply {

                        selectionModel?.selectedItemProperty()?.addListener(
                                { _, _, newSelection ->

                                    newSelection?.id?.apply { payEditBind.setSelectCategoryFromAccountTransfer() }
                                })
                    }
                }

                field("Сумма") {
                    textfield().apply {
                        bind(payEditBind.amountProperty)

                        textFormatter = currencyTextFormatter()

                    }
                 }

                field("Ремарка") {
                    textarea(payEditBind.descriptionProperty).prefRowCount = 3
                }
            }
        }
    }

    fun changeSelectEditPay(pay: Pay) {

        saveOrCancelEditPay()

        startEditPay(pay)
    }

    private fun startEditPay(pay: Pay) {

        payEditBind.initPay(pay)

        oldSavePayBind.initPay(pay)
    }

    fun saveOrCancelEditPay(selectEvent: ChangeSelectEdit? = null) {

        if(!payEditBind.isInit() || isDisableEdit().value) return

        val changeSelect = selectEvent?.let { it } ?: changeSelectEdit

        MAP_SELECT[changeSelect]?.invoke(payEditBind)
    }

    private val MAP_SELECT = mapOf<ChangeSelectEdit, (payEditBind: PayBind)->Unit >(
            ChangeSelectEdit.SAVE to PayEdit::savePay,
            ChangeSelectEdit.CANCEL to PayEdit::cancelPay,
            ChangeSelectEdit.CONFIRM to PayEdit::confirmSavePay)


    private fun confirmSavePay(payEditBind: PayBind) {
        val okType = Alert(AlertType.CONFIRMATION, SAVE_THIS_DATA, ButtonType.OK, ButtonType.NO).showAndWait()

        if(okType.isPresent && okType.get() == ButtonType.OK) {
            savePay(payEditBind)
        } else {
            cancelPay(payEditBind)
        }
    }

    private const val SAVE_THIS_DATA = "Сохранить данные предыдущей строки?"

    private fun cancelPay(payEditBind: PayBind) {

        oldSavePayBind.copyTo(payEditBind)
    }

    private fun savePay(payEditBind: PayBind) {
        try {
            val newPay = payEditBind.saveToPay()

            newPay?.apply { PayService.save(this) }

            payEditBind.copyTo(oldSavePayBind)

        } catch (e :Exception) {
            logger.error("save", e)

            alert(Alert.AlertType.ERROR, e.message ?: ALERT_ERROR_SAVE)
        }
    }

    private const val ALERT_ERROR_SAVE = "Ошибка при сохранении"
}
