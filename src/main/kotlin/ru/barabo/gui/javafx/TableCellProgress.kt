package ru.barabo.gui.javafx

import javafx.scene.control.ProgressBar
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.util.Callback
import tornadofx.box
import tornadofx.c
import tornadofx.style

class TableCellProgress<S> : TableCell<S, Double?>() {

    private val progressBar: ProgressBar = ProgressBar()

    private val text: Text = Text()

    private val stackPane: StackPane = StackPane()

    companion object {
        //private val logger = LoggerFactory.getLogger(TableCellProgress::class.java)
    }

    init {
        styleClass.clear()
        styleClass.add("progress-bar-table-cell")

        progressBar.maxWidth = Double.MAX_VALUE

        stackPane.maxWidth = Double.MAX_VALUE

        stackPane.children.setAll(progressBar, text)
    }

    override fun updateItem(item: Double?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            graphic = null
            return
        }

        val percent = item?.let{100.0 * it}

        text.text = percent?.let{"${it.toInt()}%"}?:""

        progressBar.progressProperty().unbind()

        changeProgressColor(percent)

        val column = tableColumn
        val observable = column?.getCellObservableValue(index)

        observable?.let { progressBar.progressProperty().bind(it) }
                  ?: item?.apply { progressBar.progress = this }

        graphic = stackPane
    }

    private fun changeProgressColor(percentDouble: Double?) {

        val percent = percentDouble?.toInt()?: return

        val color = when (percent) {
             in 0..30 -> { "lime"}
             in 31..80 -> { return@changeProgressColor }
             in 81..103 -> {"orange"}
             else -> {"red"}
        }

        progressBar.style {

            backgroundColor += c(color)

            baseColor = c(color)

            accentColor = c(color)

            borderColor += box(c(color))
        }
    }
}

fun <S> tableCellProgressCallback(): Callback<TableColumn<S, Double?>, TableCell<S, Double?>> = Callback{ TableCellProgress() }
