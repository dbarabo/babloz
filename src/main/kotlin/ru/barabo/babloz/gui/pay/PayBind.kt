package ru.barabo.babloz.gui.pay

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import ru.barabo.babloz.db.entity.*
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.entity.group.GroupPerson
import ru.barabo.babloz.db.entity.group.GroupProject
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.gui.binding.BindProperties
import ru.barabo.babloz.gui.formatter.fromFormatToCurrency
import ru.barabo.babloz.gui.formatter.toCurrencyFormat
import java.time.LocalDate
import java.time.LocalDateTime

internal class PayBind : BindProperties<Pay> {

    override var editValue: Pay? = null

    val accountProperty = SimpleObjectProperty<Account>()

    val accountTransferProperty = SimpleObjectProperty<Account>()

    val dateProperty = SimpleObjectProperty<LocalDate>()

    val amountProperty = SimpleStringProperty()

    val descriptionProperty = SimpleStringProperty()

    private val categoryProperty = SimpleObjectProperty<GroupCategory>()

    private val projectProperty = SimpleObjectProperty<GroupProject>()

    private val personProperty = SimpleObjectProperty<GroupPerson>()

    private var treeViewCategory: TreeView<GroupCategory>? = null

    private var treeViewProject: TreeView<GroupProject>? = null

    private var treeViewPerson: TreeView<GroupPerson>? = null

    override fun fromValue(value: Pay?) {

        accountProperty.value = value?.account

        accountTransferProperty.value = value?.accountTo

        dateProperty.value = value?.created.toDate()

        setSelectItemCategory(value?.category)

        setSelectItemProject(value?.project)

        setSelectItemPerson(value?.person)

        amountProperty.value =  toCurrencyFormat(value?.amount)

        descriptionProperty.value = value?.description
    }

    override fun toValue(value: Pay) {
        value.account = accountProperty.value

        value.accountTo = accountTransferProperty.value

        value.created = dateProperty.value.toDateTime()

        value.category = categoryProperty.value?.category

        value.project = projectProperty.value?.project

        value.person = personProperty.value?.person

        value.amount = fromFormatToCurrency(amountProperty.value)

        value.description = descriptionProperty.value
    }

    override fun copyToProperties(destination: BindProperties<Pay>) {

        val destPayBind = destination as PayBind

        destPayBind.accountProperty.value = this.accountProperty.value

        destPayBind.accountTransferProperty.value = this.accountTransferProperty.value

        destPayBind.dateProperty.value = this.dateProperty.value

        destPayBind.categoryProperty.value = this.categoryProperty.value

        destPayBind.projectProperty.value = this.projectProperty.value

        destPayBind.personProperty.value = this.personProperty.value

        destPayBind.amountProperty.value = this.amountProperty.value

        destPayBind.descriptionProperty.value = this.descriptionProperty.value
    }

    override fun isEqualsProperties(compare: BindProperties<Pay>): BooleanBinding {
        val comparePayBind = compare as PayBind

        return Bindings.and(
                accountProperty.isEqualTo(comparePayBind.accountProperty),
                accountTransferProperty.isEqualTo(comparePayBind.accountTransferProperty)
                        .and(amountProperty.isEqualTo(comparePayBind.amountProperty))
                        .and(dateProperty.isEqualTo(comparePayBind.dateProperty))
                        .and(descriptionProperty.isEqualTo(comparePayBind.descriptionProperty))
                        .and(categoryProperty.isEqualTo(comparePayBind.categoryProperty)
                        .and(projectProperty.isEqualTo(comparePayBind.projectProperty))
                        .and(personProperty.isEqualTo(comparePayBind.personProperty))
                )
        )
    }

    fun setTreeViewCategory(treeView: TreeView<GroupCategory>?) {
        treeViewCategory = treeView
    }

    fun setTreeViewProject(treeView: TreeView<GroupProject>?) {
        treeViewProject = treeView
    }

    fun setTreeViewPerson(treeView: TreeView<GroupPerson>?) {
        treeViewPerson = treeView
    }

    private fun setSelectItemCategory(category : Category?) {

        treeViewCategory?.root?.collapseItems()

        categoryProperty.value = category?.let { GroupCategory.findByCategory(it) }

        treeViewCategory?.selectedItem(categoryProperty.value)
    }

    private fun setSelectItemProject(project: Project?) {

        treeViewProject?.root?.collapseItems()

        projectProperty.value = project?.let { GroupProject.findByProject(it) }

        treeViewProject?.selectedItem(projectProperty.value)
    }

    private fun setSelectItemPerson(person: Person?) {

        treeViewPerson?.root?.collapseItems()

        personProperty.value = person?.let { GroupPerson.findByPerson(it) }

        treeViewPerson?.selectedItem(personProperty.value)
    }


    fun setSelectCategoryFromTreeView(newSelection: GroupCategory?) {

        categoryProperty.value = newSelection

        if(categoryProperty.value != GroupCategory.TRANSFER_CATEGORY) {

            accountTransferProperty.value = AccountService.NULL_ACCOUNT
        }
    }

    fun setSelectProjectFromTreeView(newSelection: GroupProject?) {

        projectProperty.value = newSelection
    }

    fun setSelectPersonFromTreeView(newSelection: GroupPerson?) {

        personProperty.value = newSelection
    }

    fun setSelectCategoryFromAccountTransfer() {
        categoryProperty.value = GroupCategory.TRANSFER_CATEGORY

        treeViewCategory?.selectedItem(categoryProperty.value)
    }

    private fun LocalDateTime?.toDate() = this?.toLocalDate()?: LocalDate.now()

    private fun LocalDate.toDateTime() = if(this == LocalDate.now()) LocalDateTime.now() else this.atStartOfDay()
}

private fun TreeItem<*>.collapseItems() {

    this.children.forEach { if(it.isExpanded) it.isExpanded = false }
}

private fun <T> TreeView<T>.selectedItem(item: T?) {

    this.root?.findTreeItem(item)?.let { this.selectionModel?.select(it) }
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