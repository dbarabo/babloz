package ru.barabo.babloz.db.service.report

import java.time.LocalDate

interface CallBackUpdateListener<T> {

    val listeners: MutableList<()->Unit>

    fun addListener(listener: ()->Unit) {
        listeners += listener
    }

    fun updateInfoListeners() {
        listeners.forEach{ it() }
    }

    fun dateListenerList():  List<LocalDate>

    fun infoMap(): Map<T, IntArray>
}