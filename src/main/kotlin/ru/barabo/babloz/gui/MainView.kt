package ru.barabo.babloz.gui

import javafx.scene.control.TabPane
import javafx.scene.control.ToolBar
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import ru.barabo.babloz.main.ResourcesManager
import tornadofx.App
import tornadofx.View
import tornadofx.launch
import tornadofx.plusAssign

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

class MainView: View() {

    override var root : VBox = VBox()

    private val toolBar = ToolBar()

    private val tabPane :TabPane = TabPane()

    init {
        title = "Babloz"

        tabPane.tabs.add(AccountList)

        root += tabPane

        VBox.setVgrow(tabPane, Priority.ALWAYS)
    }
}



