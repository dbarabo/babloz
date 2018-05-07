package ru.barabo.babloz.gui.pay.filter

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.ComboBox
import org.controlsfx.control.CheckComboBox

class ComboxFilter<T>(items: ObservableList<T>?, mainItem: T, filtered: (List<T>)-> Unit, allList: ()->List<T>) : CheckComboBox<T>(items) {

    init {
        maxWidth = 100.0

        prefWidth = maxWidth

        checkModel.check(0)

        addEventHandler(ComboBox.ON_HIDDEN, {

            val checkedItems = checkModel.checkedItems

            if(checkedItems.isEmpty() || mainItem in checkedItems) {
                filtered(emptyList())
            } else {
                filtered(checkedItems)
            }
        })

        checkModel.checkedItems.addListener(ListChangeListener {
            while (it.next()) {
                if(mainItem in it.addedSubList) {
                    allList().forEach { checkModel.check(it) }
                    return@ListChangeListener
                }

                if(mainItem in it.removed) {
                    allList().forEach { checkModel.clearCheck(it) }
                    return@ListChangeListener
                }
            }
        })
    }
}