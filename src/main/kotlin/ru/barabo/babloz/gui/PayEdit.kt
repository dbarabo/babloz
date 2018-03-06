package ru.barabo.babloz.gui

import com.sun.javafx.tk.Toolkit
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.GroupCategory
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.gui.custom.ChangeSelectEdit
import ru.barabo.babloz.gui.formatter.currencyTextFormatter
import ru.barabo.babloz.gui.formatter.fromFormatToCurrency
import ru.barabo.babloz.gui.formatter.toCurrencyFormat
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime


object PayEdit: VBox() { //Tab("Правка платежа", VBox()) {

    private val logger = LoggerFactory.getLogger(PayEdit::class.java)

    private const val ROW_COUNT = 9.0

    private var changeSelectEdit = ChangeSelectEdit.SAVE

    private val payEditBind = PayBind()

    private val newPayOldBind = PayBind()

    fun isDisableEdit() = payEditBind.isDisableEdit(newPayOldBind)

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

        newPayOldBind.initPay(pay)
    }

    fun saveOrCancelEditPay(selectEvent: ChangeSelectEdit? = null) {

        if(!payEditBind.isInit() || isDisableEdit().value) return

        val changeSelect = selectEvent?.let { it } ?: changeSelectEdit

        MAP_SELECT[changeSelect]?.invoke(payEditBind)
    }

    private val MAP_SELECT = mapOf<ChangeSelectEdit, (payEditBind: PayBind)->Unit >(
            ChangeSelectEdit.SAVE to ::savePay,
            ChangeSelectEdit.CANCEL to ::cancelPay,
            ChangeSelectEdit.CONFIRM to ::confirmSavePay)


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

        newPayOldBind.copyTo(payEditBind)
    }

    private fun savePay(payEditBind: PayBind) {
        try {
            val newPay = payEditBind.saveToPay()

            newPay?.apply { PayService.save(this) }

            payEditBind.copyTo(newPayOldBind)

        } catch (e :Exception) {
            logger.error("save", e)

            alert(Alert.AlertType.ERROR, e.message ?: ALERT_ERROR_SAVE)
        }
    }

    private const val ALERT_ERROR_SAVE = "Ошибка при сохранении"
}

internal class PayBind {
    val accountProperty = SimpleObjectProperty<Account>()

    val accountTransferProperty = SimpleObjectProperty<Account>()

    val dateProperty = SimpleObjectProperty<LocalDate>()

    val amountProperty = SimpleStringProperty()

    val descriptionProperty = SimpleStringProperty()

    private val categoryProperty = SimpleObjectProperty<GroupCategory>()

    private var treeViewCategory: TreeView<GroupCategory>? = null

    private var editPay: Pay? = null

    fun setTreeViewCategory(treeView: TreeView<GroupCategory>?) {
        treeViewCategory = treeView
    }

    fun isDisableEdit(comparePayBind: PayBind) = Bindings.and(
            accountProperty.isEqualTo(comparePayBind.accountProperty),
            accountTransferProperty.isEqualTo(comparePayBind.accountTransferProperty)
                    .and(amountProperty.isEqualTo(comparePayBind.amountProperty))
                    .and(dateProperty.isEqualTo(comparePayBind.dateProperty))
                    .and(descriptionProperty.isEqualTo(comparePayBind.descriptionProperty))
                    .and(categoryProperty.isEqualTo(comparePayBind.categoryProperty))
    )!!

    private fun setSelectItem(category : Category?) {

        treeViewCategory?.root?.collapseItems()

        categoryProperty.value = category?.let { GroupCategory.findByCategory(it) }

        treeViewCategory?.selectedItem(categoryProperty.value)
    }

    private fun TreeView<GroupCategory>.selectedItem(item: GroupCategory?) {

        this.root?.findTreeItem(item)?.let { this.selectionModel?.select(it) }
    }

    fun setSelectCategoryFromTreeView(newSelection: GroupCategory?) {

        categoryProperty.value = newSelection

        if(categoryProperty.value != GroupCategory.TRANSFER_CATEGORY) {

            accountTransferProperty.value = AccountService.NULL_ACCOUNT
        }
    }

    fun setSelectCategoryFromAccountTransfer() {
        categoryProperty.value = GroupCategory.TRANSFER_CATEGORY

        treeViewCategory?.selectedItem(categoryProperty.value)
    }

    fun isInit() = editPay != null

    fun initPay(pay: Pay) {

        editPay = pay

        accountProperty.value = pay.account

        accountTransferProperty.value = pay.accountTo

        dateProperty.value = pay.created.toDate()

        setSelectItem(pay.category)

        amountProperty.value =  toCurrencyFormat(pay.amount)

        descriptionProperty.value = pay.description
    }

    private fun LocalDateTime?.toDate() = this?.toLocalDate()?:LocalDate.now()

    private fun LocalDate.toDateTime() = if(this == LocalDate.now()) LocalDateTime.now() else this.atStartOfDay()

    fun copyTo(destPayBind: PayBind) {
        destPayBind.accountProperty.value = this.accountProperty.value

        destPayBind.accountTransferProperty.value = this.accountTransferProperty.value

        destPayBind.dateProperty.value = this.dateProperty.value

        destPayBind.categoryProperty.value = this.categoryProperty.value

        destPayBind.amountProperty.value = this.amountProperty.value

        destPayBind.descriptionProperty.value = this.descriptionProperty.value
    }

    fun saveToPay(): Pay? {
        if(editPay == null) throw  Exception("editPay is null")

        val newPay = editPay!!

        newPay.account = accountProperty.value

        newPay.accountTo = accountTransferProperty.value

        newPay.created = dateProperty.value.toDateTime()

        newPay.category = categoryProperty.value?.category

        newPay.amount = fromFormatToCurrency(amountProperty.value)

        newPay.description = descriptionProperty.value

        return newPay
    }
}

private fun TreeItem<*>.collapseItems() {

    this.children.forEach { if(it.isExpanded) it.isExpanded = false }
}

private fun <T> TreeItem<T>.findTreeItem(childItem: T): TreeItem<T>? {

    if(this.value === childItem) return this

    for (child in children) {
        val find = child.findTreeItem(childItem)

        if(find != null) {
            return find
        }
    }
    return null
}