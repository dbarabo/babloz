package ru.barabo.db.service

import ru.barabo.db.EditType
import ru.barabo.db.SessionException
import ru.barabo.db.TemplateQuery

abstract class StoreService<T :Any, G>(private val orm :TemplateQuery) {

    private val listenerList = ArrayList<StoreListener<G>>()

    protected val dataList = ArrayList<T>()

    init {
        readData()
    }

    abstract protected fun elemRoot() :G

    abstract protected fun clazz() :Class<T>

    open protected fun processInsert(item :T) {}

    open protected fun processUpdate(item :T) {}

    open protected fun beforeRead() {}

    private fun callBackSelectData(item :T) {

        synchronized(dataList) { dataList.add(item) }

        processInsert(item)
    }

    fun addListener(listener : StoreListener<G>) {
        listenerList.add(listener)

        listener.refreshAll(elemRoot())
    }

    private fun sentRefreshAllListener() {
        listenerList.forEach {it.refreshAll(elemRoot())}
    }

    private fun readData() {
        synchronized(dataList) { dataList.clear() }

        beforeRead()

        orm.select(clazz(), ::callBackSelectData)

        sentRefreshAllListener()
    }

    @Throws(SessionException::class)
    fun save(item :T) :T {

        val type = orm.save(item)

        when (type) {
            EditType.INSERT -> {
                dataList.add(item)

                processInsert(item)
            }
            EditType.EDIT -> {
                processUpdate(item)
            }
        }

        sentRefreshAllListener()

        return item
    }

    //inline fun <reified T : Any>T.clazz() = T::class.java
}

