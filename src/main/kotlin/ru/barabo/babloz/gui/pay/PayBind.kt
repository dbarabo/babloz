package ru.barabo.babloz.gui.pay

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.GroupCategory
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.gui.formatter.fromFormatToCurrency
import ru.barabo.babloz.gui.formatter.toCurrencyFormat
import java.time.LocalDate
import java.time.LocalDateTime

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

    private fun LocalDateTime?.toDate() = this?.toLocalDate()?: LocalDate.now()

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