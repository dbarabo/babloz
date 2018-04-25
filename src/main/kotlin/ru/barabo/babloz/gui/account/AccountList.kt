package ru.barabo.babloz.gui.account

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.GroupAccount
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.gui.custom.ChangeSelectEdit
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.service.StoreListener
import tornadofx.*

object AccountList : Tab("Счета", VBox()), StoreListener<GroupAccount> {

    private var splitPane: SplitPane? = null

    private var treeTable : TreeTableView<GroupAccount>? = null

    init {
        form {
            toolbar {
                button ("Новый", ResourcesManager.icon("new.png")).apply {
                   setOnAction { showNewAccount() }
                }
                separator {  }

                button ("Сохранить", ResourcesManager.icon("save.png")).apply {
                    setOnAction { saveAccount() }

                    disableProperty().bind(AccountSaver.isDisableEdit())
                }
                button ("Отменить", ResourcesManager.icon("cancel.png")).apply {
                    setOnAction { cancelAccount() }

                    disableProperty().bind(AccountSaver.isDisableEdit())
                }
            }
            splitpane(Orientation.HORIZONTAL, AccountEdit).apply { splitPane = this }
        }
        VBox.setVgrow(splitPane, Priority.ALWAYS)

        AccountService.addListener(this)
    }

    private fun showNewAccount() {

        AccountSaver.changeSelectEditValue(Account())
    }

    private fun saveAccount() {

        AccountSaver.saveOrCancelEdit(ChangeSelectEdit.SAVE)

        treeTable?.requestFocus()
    }

    private fun cancelAccount() {

        AccountSaver.saveOrCancelEdit(ChangeSelectEdit.CANCEL)

        treeTable?.requestFocus()
    }

    override fun refreshAll(elemRoot: GroupAccount) {

        treeTable?.let {
            Platform.runLater({ run {it.refresh()} })
            return
        }

        Platform.runLater({
            run {
                treeTable?.let { splitPane?.items?.remove(it) }

                synchronized(elemRoot) {
                    treeTable = treeTable(elemRoot)
                }

                treeTable?.root?.children?.forEach { it.expandedProperty().set(true) }

                treeTable?.selectionModel?.selectedItemProperty()?.addListener(
                        { _, _, newSelection ->
                            AccountSaver.changeSelectEditValue(
                                    if(newSelection?.value?.parent !== GroupAccount.root)newSelection?.value?.account else null)
                        })

                splitPane?.items?.add(treeTable)

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