package ru.barabo.babloz.gui

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Tab
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.GroupAccount
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.service.StoreListener
import tornadofx.*

object AccountList : Tab("Счета", VBox()), StoreListener<GroupAccount> {

    private var forma :Form? = null

    private var treeTable : TreeTableView<GroupAccount>? = null

    private var selectGroupAccount :GroupAccount? = null

    init {
        this.graphic = ResourcesManager.icon("account.png")

        forma = form {
            toolbar {
                button ("Новый счет", ResourcesManager.icon("new.png")).setOnAction { showNewAccount() }

                button ("Правка счета", ResourcesManager.icon("edit.png")).setOnAction { showEditAccount() }
            }
        }
        AccountService.addListener(this)
    }

    private fun showAccount() {
        if(!tabPane.tabs.contains(AccountEdit)) {
            tabPane.tabs.add(AccountEdit)
        }
    }

    private val ALERT_ACCOUNT_NOT_SELECT = "Встаньте на изменеямый счет в таблице счетов"

    private fun showEditAccount() {

        selectGroupAccount?.account?.id
                ?. let { showAccount()
                         AccountEdit.editAccount(selectGroupAccount!!.account) }

                ?: alert(Alert.AlertType.ERROR, ALERT_ACCOUNT_NOT_SELECT)
    }

    private fun showNewAccount() {
        showAccount()

        AccountEdit.editAccount(selectGroupAccount!!.account.copy(id = null, name = "",
                description = null, closed = null, rest = null))
    }

    override fun refreshAll(elemRoot: GroupAccount) {

        Platform.runLater({
            run {
                treeTable?.removeFromParent()

                synchronized(elemRoot) {
                    treeTable = treeTable(elemRoot)
                }

                treeTable?.root?.children?.forEach { it.expandedProperty().set(true) }

                treeTable?.selectionModel?.selectedItemProperty()?.addListener(
                        { _, _, newSelection ->
                            selectGroupAccount = newSelection?.value
                        })

                forma?.addChildIfPossible(treeTable!!)

                VBox.setVgrow(treeTable, Priority.ALWAYS)
            }
        })
    }

    private fun treeTable(rootGroup :GroupAccount) :TreeTableView<GroupAccount> {
        return TreeTableView<GroupAccount>().apply {
            column("Счет", GroupAccount::name)

            column("Остаток", GroupAccount::rest)

            root = TreeItem(rootGroup)

            populate { it.value.child }

            root.isExpanded = true

            this.isShowRoot = false

            this.resizeColumnsToFitContent()
        }
    }
}