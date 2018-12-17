package ru.barabo.babloz.db.service.report

import java.time.LocalDate

interface CallBackUpdateListener {

    val listeners: MutableList<(List<LocalDate>, Map<*,IntArray>)->Unit>

    fun addListener(listener: (List<LocalDate>, Map<*,IntArray>)->Unit) {
        listeners += listener
    }

    fun updateInfoListeners() {
        listeners.forEach{ it(dateListenerList(), infoMap()) }
    }

    fun dateListenerList():  List<LocalDate>

    fun infoMap(): Map<*,IntArray>
}