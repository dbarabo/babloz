package ru.barabo.babloz.gui

import javafx.geometry.Orientation
import javafx.scene.control.TabPane
import javafx.scene.control.ToggleGroup
import javafx.scene.control.ToolBar
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.stage.Screen
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import ru.barabo.babloz.gui.pay.PayList
import ru.barabo.babloz.main.ResourcesManager
import tornadofx.*

fun startLaunch(args :Array<String>) = launch<MainApp>(args)

class MainApp: App(MainView::class) {

    val logger = LoggerFactory.getLogger(MainApp::class.java)!!

    override fun start(stage: Stage) {

        super.start(stage)

        val screen = Screen.getPrimary()

        stage.icons.add(ResourcesManager.image("babloz64.png"))

        stage.x = screen.visualBounds.minX
        stage.y = screen.visualBounds.minY

        stage.width = screen.visualBounds.width / 3 * 2

        stage.height = screen.visualBounds.height
    }

    override fun stop() {

        super.stop()
    }
}

class MainView: View() {

    override var root : HBox = HBox()

    private val toolBar = ToolBar()

    private val tabPane :TabPane = TabPane()

    init {
        title = "Babloz"

        tabPane.tabMaxHeight = 0.0
        tabPane.tabMaxWidth = 0.0

        tabPane.tabs.add(AccountList)

        val toggleGroup = ToggleGroup()

        root +=  toolbar {

            orientation = Orientation.VERTICAL

            label(graphic = ResourcesManager.icon("babloz.png"))

            togglebutton ("Счета", toggleGroup).apply {

                graphic = ResourcesManager.icon("account.png")

                setOnAction { tabPane.selectionModel.select(AccountList) }

                prefWidth = 150.0
            }

            togglebutton ("Платежи", toggleGroup).apply {

                graphic = ResourcesManager.icon("pay.png")

                setOnAction{ showPay() }

                prefWidth = 150.0
            }

//            button ("Платежи", ResourcesManager.icon("edit.png")).setOnAction { AccountList.showEditAccount() }
//
//            separator {  }
//
//            button ("Категории", ResourcesManager.icon("tree.png")).setOnAction { AccountList.showCategoryList() }
//
//            button ("Проекты", ResourcesManager.icon("tree.png")).setOnAction { AccountList.showPayList() }
        }

        root += tabPane

        HBox.setHgrow(tabPane, Priority.ALWAYS)
    }

    private fun showPay() {
        if(!tabPane.tabs.contains(PayList)) {
            tabPane.tabs.add(PayList)
        }

        tabPane.selectionModel.select(PayList)
    }
}



