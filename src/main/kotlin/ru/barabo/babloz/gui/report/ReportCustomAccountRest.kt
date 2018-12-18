package ru.barabo.babloz.gui.report

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.chart.XYChart
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.ComboBox
import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import org.controlsfx.control.CheckTreeView
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.group.GroupAccount
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.report.ChangeEntity
import ru.barabo.babloz.db.service.report.PeriodType
import ru.barabo.babloz.db.service.report.restaccount.AccountViewType
import ru.barabo.babloz.db.service.report.restaccount.ReportServiceRestAccounts
import ru.barabo.babloz.gui.pay.PayEdit.rowHeight
import ru.barabo.babloz.gui.pay.comboBoxDates
import ru.barabo.babloz.gui.pay.defaultRowCount
import tornadofx.*
import java.time.LocalDate
import kotlin.reflect.KMutableProperty0

object ReportCustomAccountRest : Tab("Остатки на счетах", VBox()), ChangeTabSelected {

    init {
        form {

            fieldset("Категории отчета") {
                addChildIfPossible(checkTreeAccount(ReportServiceRestAccounts))
            }

            fieldset("Период отчета") {
                comboBoxDates(ReportServiceRestAccounts::setDateRange)
            }

            fieldset("Показывать типы счетов") {
                comboBoxCustomAccount(ReportServiceRestAccounts::accountViewType.setter)
            }

            fieldset("Вид диаграммы") {
                comboBoxDiagramViewType(ReportAccountRestDrawChart::diagramViewType)
            }
        }
    }

    override fun processSelect() {
        ReportServiceRestAccounts.updateInfoListeners()
    }
}

object ReportAccountRestDrawChart {

    var diagramViewType: DiagramViewType = DiagramViewType.LINE_CHART
    set(value) {
        field = value

        ReportServiceRestAccounts.updateInfoListeners()
    }

    fun drawChart(chartMap: Map<DiagramViewType, XYChart<String, Number>>): XYChart<String, Number> {

        val chart = chartMap[diagramViewType]!!

        val dates = ReportServiceRestAccounts.datePeriods

        val accountRests = ReportServiceRestAccounts.infoMap()

        return chart.drawChart(dates, accountRests, { it.name}, ReportServiceRestAccounts.periodType)
    }
}

internal fun <T> XYChart<String, Number>.drawChart(dates: List<LocalDate>, mapData: Map<T, IntArray>,
                                          entityName: (T)->String?, periodType: PeriodType): XYChart<String, Number> {
    for(entity in mapData.keys) {
        val line = XYChart.Series<String, Number>().apply { name = entityName(entity) }

        val turn = mapData[entity]

        for((index, date) in dates.withIndex()) {
            line.data.add(XYChart.Data(periodType.format(date), turn?.get(index)) )
        }

        data.add(line)
    }

    return this
}

fun EventTarget.comboBoxDiagramViewType(propertyDiagramTypeView: KMutableProperty0<DiagramViewType>): ComboBox<DiagramViewType> {
    val selectProperty = SimpleObjectProperty<DiagramViewType>(DiagramViewType.LINE_CHART)

    return combobox<DiagramViewType>(property = selectProperty, values = DiagramViewType.values().toList()).apply {

        selectionModel?.selectedItemProperty()?.addListener { _, _, newSelection ->

            propertyDiagramTypeView.setter(newSelection)
        }
    }
}

fun EventTarget.comboBoxCustomAccount(processAccountView: (AccountViewType)->Unit): ComboBox<AccountViewType> {

    val dateSelectProperty = SimpleObjectProperty<AccountViewType>(AccountViewType.BALANCE)

    return combobox<AccountViewType>(property = dateSelectProperty, values = AccountViewType.values().toList()).apply {

        selectionModel?.selectedItemProperty()?.addListener { _, _, newSelection ->

            processAccountView(newSelection)
        }
    }
}

internal fun checkTreeAccount(changeEntity: ChangeEntity<Account>) =
        CheckTreeView<GroupAccount>(CheckBoxTreeItem<GroupAccount>(AccountService.accountRoot()) ).apply {

            this.populate (itemFactory = {
                CheckBoxTreeItem(it, null, it.account.isSelected ?:0 != 0).apply {

                    selectedProperty().addListener { _, _, newValue ->

                        if(newValue == true) {
                            changeEntity.addEntity(this.value.account)
                        } else {
                            changeEntity.removeEntity(this.value.account)
                        }
                    }
                }
            },

                    childFactory = { it.value.child })

            this.isShowRoot = false

            this.root.isExpanded = true

            prefHeight = rowHeight() * defaultRowCount(GroupAccount.countRoot()) * 2
        }