package ru.barabo.babloz.db.service.report

interface CallBackUpdateListener<T> {

    val listeners: MutableList<()->Unit>

    fun addListener(listener: ()->Unit) {
        listeners += listener
    }

    fun updateInfoListeners() {
        listeners.forEach{ it() }
    }

    fun infoMap(): Map<T, IntArray>
}