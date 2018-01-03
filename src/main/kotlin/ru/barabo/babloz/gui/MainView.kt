package ru.barabo.babloz.gui

import javafx.application.Platform
import javafx.scene.control.MenuBar
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.GroupAccount
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.StoreListener
import ru.barabo.babloz.main.ResourcesManager
import tornadofx.*

fun startLaunch(args :Array<String>) = launch<MainApp>(args)

class MainApp: App(MainView::class) {

    val logger = LoggerFactory.getLogger(MainApp::class.java)!!

    override fun start(stage: Stage) {

        super.start(stage)

        val screen = Screen.getPrimary()

        stage.icons.add(ResourcesManager.image("babloz.png"))

        stage.x = screen.visualBounds.minX
        stage.y = screen.visualBounds.minY

        stage.width = screen.visualBounds.width / 3 * 2

        stage.height = screen.visualBounds.height
    }

    override fun stop() {

        super.stop()
    }
}

class MainView: View(), StoreListener<GroupAccount> {

    override var root : VBox = VBox()

    private var treeTable : TreeTableView<GroupAccount>? = null

    private val menuBar = MenuBar()

    init {
        title = "Babloz"

        root += menuBar

        AccountService.addListener(this)
    }

    override fun refreshAll(rootElem: GroupAccount) {

        Platform.runLater({
            run {
                treeTable?.removeFromParent()

                synchronized(root) {
                    treeTable = treeTable(rootElem)
                }

                treeTable?.root?.children?.forEach {
                    it.expandedProperty().set(true)
                    it?.children?.forEach { it.expandedProperty().set(true) }
                }

                treeTable?.selectionModel?.selectedItemProperty()?.addListener(
                        { obs, oldSelection, newSelection ->

                           // actionMenu.groupElem = newSelection?.value
                        })

                root += treeTable!!

                VBox.setVgrow(treeTable, Priority.ALWAYS)

                treeTable?.root?.children?.forEach {
                    it?.children?.forEach { it.expandedProperty().set(false) }
                }

//                val fontMetrics = Toolkit.getToolkit().fontLoader.getFontMetrics(treeTable?.columns?.get(0)?.label()?.font)
//
//                val defaultValues = arrayOf("Проверка отправки ПТК ПСД", "В Архиве",
//                        "KESDT_0021_0000_20171227_001.ARJ", "1171576789", "23:59:59", "23:59:59", "9999", "Нет ошибок Да")
//
//                defaultValues.indices.forEach {
//                    treeTable?.columns?.get(it)?.prefWidth = fontMetrics.computeStringWidth(defaultValues[it]).toDouble() + 5
//                }
            }
        })
    }
}



private fun getColorBackGround(isConfig :Boolean, isSelected :Boolean) :String {

    return if(isConfig) "-fx-background-color: lightgray;" else "-fx-background-color: white;"
}

private fun getFontStyle(isEmptyChild :Boolean) :String {
    return if(isEmptyChild) "" else "-fx-font-weight: bold;"
}

private fun treeTable(rootGroup :GroupAccount) :TreeTableView<GroupAccount> {
    return TreeTableView<GroupAccount>().apply {
        column("Счет", GroupAccount::name)

        column("Остаток", GroupAccount::rest)
//                .cellFormat {
//            val treeItem = treeTableView.getTreeItem(index)
//
//            text = it
//
//            treeTableView.selectionModel.focusedIndex
//
//            textFill = getFontColor(treeItem.value.elem.state, treeItem.value.isConfig)
//
//            style += getFontStyle(treeItem.value.child.isEmpty())
//
//            style += getColorBackGround(treeItem.value.isConfig, isSelected)
//        }


        root = TreeItem(rootGroup)

        populate { it.value.child }

        root.isExpanded = true

        this.isShowRoot = false

        this.resizeColumnsToFitContent()
    }
}
