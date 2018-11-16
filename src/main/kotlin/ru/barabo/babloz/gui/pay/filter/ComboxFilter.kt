package ru.barabo.babloz.gui.pay.filter

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.ComboBox
import org.controlsfx.control.CheckComboBox

class ComboxFilter<T>(items: ObservableList<T>?, mainItem: T, filtered: (List<T>)-> Unit, allList: ()->List<T>) : CheckComboBox<T>(items) {

    @Volatile private var isUncheckedMainItem = 0

    init {
        maxWidth = 100.0

        prefWidth = maxWidth

        //checkModel.check(0)

        addEventHandler(ComboBox.ON_HIDDEN) {

            val checkedItems = checkModel.checkedItems

            if(checkedItems.isEmpty() || mainItem in checkedItems) {
                filtered(emptyList())
            } else {
                filtered(checkedItems)
            }
        }

        checkModel.checkedItems.addListener( ListChangeListener { change ->

            while (change.next()) {

                if(mainItem in change.addedSubList) {
                    allList().forEach { checkModel.check(it) }
                    break
                }

                if(mainItem in change.removed) {
                    if(isUncheckedMainItem > 0) {
                        isUncheckedMainItem--
                    } else {
                        allList().forEach { if(it in checkModel.checkedItems) checkModel.clearCheck(it) }
                    }
                    break
                }

                if(change.removed.size > 0) {
                    val checkedItems = checkModel.checkedItems ?: return@ListChangeListener

                    if(mainItem in checkedItems && mainItem !in change.removed) {
                        isUncheckedMainItem = 2
                        break
                    }
                }
            }

            if(isUncheckedMainItem == 2) {
                checkModel.clearCheck(mainItem)
            }
        })
    }
}