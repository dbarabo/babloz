package ru.barabo.babloz.db.service.report

import java.time.LocalDate

interface CallBackUpdateListener<T> {

    val listeners: MutableList<(List<LocalDate>, Map<T,IntArray>)->Unit>

    fun addListener(listener: (List<LocalDate>, Map<T, IntArray>)->Unit) {
        listeners += listener
    }

    fun updateInfoListeners() {
        listeners.forEach{ it(dateListenerList(), infoMap()) }
    }

    fun dateListenerList():  List<LocalDate>

    fun infoMap(): Map<T,IntArray>
}