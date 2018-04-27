package ru.barabo.babloz.gui

import javafx.geometry.Orientation
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.stage.Screen
import javafx.stage.Stage
import ru.barabo.babloz.gui.account.AccountList
import ru.barabo.babloz.gui.category.CategoryList
import ru.barabo.babloz.gui.pay.PayList
import ru.barabo.babloz.gui.person.PersonList
import ru.barabo.babloz.gui.project.ProjectList
import ru.barabo.babloz.main.ResourcesManager
import tornadofx.*

fun startLaunch(args :Array<String>) = launch<MainApp>(args)

class MainApp: App(MainView::class) {

    override fun start(stage: Stage) {

        super.start(stage)

        val screen = Screen.getPrimary()

        stage.icons.add(ResourcesManager.image("babloz64.png"))

        stage.x = screen.visualBounds.minX
        stage.y = screen.visualBounds.minY

        stage.width = screen.visualBounds.width / 3 * 2

        stage.height = screen.visualBounds.height
    }
}

class MainView: View() {

    override var root: HBox = HBox()

    private val mainTabPane: TabPane = TabPane()

    init {
        title = "Babloz"

        mainTabPane.tabMaxHeight = 0.0
        mainTabPane.tabMaxWidth = 0.0

        mainTabPane.tabs.add(AccountList)

        val toggleGroup = ToggleGroup()

        root += toolbar {

            orientation = Orientation.VERTICAL

            label(graphic = ResourcesManager.icon("babloz.png"))

            togglebutton("Счета", toggleGroup).apply {

                graphic = ResourcesManager.icon("account.png")

                setOnAction { AccountList.selectTab() }

                prefWidth = 150.0
            }

            togglebutton("Платежи", toggleGroup).apply {

                graphic = ResourcesManager.icon("pay.png")

                setOnAction { PayList.selectTab() }

                prefWidth = 150.0
            }

            label("\n")

            togglebutton("Категории", toggleGroup).apply {

                graphic = ResourcesManager.icon("tree.png")

                setOnAction { CategoryList.selectTab() }

                prefWidth = 150.0
            }

            togglebutton("Проекты", toggleGroup).apply {

                graphic = ResourcesManager.icon("project.png")

                setOnAction { ProjectList.selectTab() }

                prefWidth = 150.0
            }

            togglebutton("Субъекты", toggleGroup).apply {

                graphic = ResourcesManager.icon("person.png")

                setOnAction { PersonList.selectTab() }

                prefWidth = 150.0
            }
        }

        root += mainTabPane

        HBox.setHgrow(mainTabPane, Priority.ALWAYS)
    }

    private fun Tab.selectTab() {
        if(!mainTabPane.tabs.contains(this)) {
            mainTabPane.tabs.add(this)
        }
        mainTabPane.selectionModel.select(this)
    }
}





