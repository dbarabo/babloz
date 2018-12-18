package ru.barabo.babloz.gui.report

import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Screen
import org.slf4j.LoggerFactory
import ru.barabo.babloz.gui.MainView
import tornadofx.form
import tornadofx.splitpane

object ReportTab : Tab("Отчеты", VBox()) {

    private val customTabPane: TabPane = TabPane()

    private lateinit var splitPane: SplitPane

    private val logger = LoggerFactory.getLogger(ReportTab::class.java)!!

    init {

        form {
            splitpane(Orientation.HORIZONTAL, customTabPane, FactoryChart).apply { splitPane = this }
        }

        customTabPane.tabs.add(ReportCustomCategoryTurn)

        customTabPane.tabs.add(ReportCustomCostsIncomeTurn)

        customTabPane.tabs.add(ReportCustomAccountRest)

        customTabPane.selectionModel.selectedItemProperty().addListener {_, _, newTab->
             if (newTab is ChangeTabSelected) newTab.processSelect()
        }

        VBox.setVgrow(splitPane, Priority.ALWAYS)

        val minDiv = 3.5 * MainView.payButton.width / Screen.getPrimary().visualBounds.width

        splitPane.setDividerPosition(0, minDiv)
    }
}
