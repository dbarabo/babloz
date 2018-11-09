package ru.barabo.babloz.gui

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.stage.Screen
import javafx.stage.Stage
import ru.barabo.babloz.gui.account.AccountList
import ru.barabo.babloz.gui.budget.BudgetList
import ru.barabo.babloz.gui.category.CategoryList
import ru.barabo.babloz.gui.dialog.LoginDb
import ru.barabo.babloz.gui.pay.PayList
import ru.barabo.babloz.gui.person.PersonList
import ru.barabo.babloz.gui.project.ProjectList
import ru.barabo.babloz.gui.service.ServiceTab
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.babloz.sync.SyncZip
import tornadofx.*

fun startLaunch(args :Array<String>) = launch<MainApp>(args)

class MainApp: App(MainView::class) {

    @Volatile
    private var isExit = false

    override fun start(stage: Stage) {

        startLogin()

        super.start(stage)

        val screen = Screen.getPrimary()

        stage.icons.add(ResourcesManager.image("babloz64.png"))

        stage.x = screen.visualBounds.minX
        stage.y = screen.visualBounds.minY

        stage.width = screen.visualBounds.width / 3 * 2

        stage.height = screen.visualBounds.height
    }

    private fun startLogin() = LoginDb.startSyncDialog {
        if(!it.isSuccess) {
            isExit = true
            exitApplication()
        }
    }

    private fun exitApplication() {
        Platform.exit()
        System.exit(0)
    }

    override fun stop() {

        if(!isExit) {
            SyncZip.endSync()
        }

        super.stop()
    }
}

class MainView: View() {

    override var root: HBox = HBox()

    private val mainTabPane: TabPane = TabPane()

    companion object {
        private const val WIDTH_RIGHT_PANEL = 120.0

        private lateinit var currentMainTab: TabPane

        private lateinit var payButton: ToggleButton

        private fun Tab.selectTab() {
            if(!currentMainTab.tabs.contains(this)) {
                currentMainTab.tabs.add(this)
            }
            currentMainTab.selectionModel.select(this)
        }

        fun selectPayTab() {
            if(!payButton.isSelected) {
                payButton.isSelected = true
            }
            PayList.selectTab()
        }
    }

    init {

        title = "Babloz"

        currentMainTab = mainTabPane

        mainTabPane.tabMaxHeight = 0.0
        mainTabPane.tabMaxWidth = 0.0

        mainTabPane.tabs.add(AccountList)

        val toggleGroup = ToggleGroup()

        root += toolbar {

            orientation = Orientation.VERTICAL

            label(graphic = ResourcesManager.icon("babloz.png")).apply {
                prefWidth = WIDTH_RIGHT_PANEL
            }

            togglebutton("Счета", toggleGroup).apply {

                graphic = ResourcesManager.icon("account.png")

                setOnAction { AccountList.selectTab() }

                prefWidth = WIDTH_RIGHT_PANEL
            }

            togglebutton("Платежи", toggleGroup).apply {

                payButton = this

                graphic = ResourcesManager.icon("pay.png")

                setOnAction { PayList.selectTab() }

                prefWidth = WIDTH_RIGHT_PANEL
            }

            label("\n")

            togglebutton("Категории", toggleGroup).apply {

                graphic = ResourcesManager.icon("tree.png")

                setOnAction { CategoryList.selectTab() }

                prefWidth = WIDTH_RIGHT_PANEL
            }

            togglebutton("Проекты", toggleGroup).apply {

                graphic = ResourcesManager.icon("project.png")

                setOnAction { ProjectList.selectTab() }

                prefWidth = WIDTH_RIGHT_PANEL
            }

            togglebutton("Субъекты", toggleGroup).apply {

                graphic = ResourcesManager.icon("person.png")

                setOnAction { PersonList.selectTab() }

                prefWidth = WIDTH_RIGHT_PANEL
            }

            label("\n")

            togglebutton("Бюджет", toggleGroup).apply {

                graphic = ResourcesManager.icon("budget.png")

                setOnAction { BudgetList.selectTab() }

                prefWidth = WIDTH_RIGHT_PANEL
            }

            togglebutton("Настройки", toggleGroup).apply {

                graphic = ResourcesManager.icon("service.png")

                setOnAction { ServiceTab.selectTab() }

                prefWidth = WIDTH_RIGHT_PANEL
            }
        }

        root += mainTabPane

        HBox.setHgrow(mainTabPane, Priority.ALWAYS)
    }
}




