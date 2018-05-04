package ru.barabo.db.service

import ru.barabo.db.EditType
import ru.barabo.db.SessionException
import ru.barabo.db.SessionSetting
import ru.barabo.db.TemplateQuery
import tornadofx.observable

abstract class StoreService<T: Any, G>(private val orm :TemplateQuery) {

    private val listenerList = ArrayList<StoreListener<G>>()

    protected val dataList = ArrayList<T>().observable()

    init {
        readData()
    }

    protected abstract fun elemRoot() :G

    protected abstract fun clazz() :Class<T>

    protected open fun processInsert(item :T) {}

    protected open fun processUpdate(item :T) {}

    protected open fun beforeRead() {}

    private fun callBackSelectData(item :T) {

        synchronized(dataList) { dataList.add(item) }

        processInsert(item)
    }

    fun addListener(listener : StoreListener<G>) {
        listenerList.add(listener)

        listener.refreshAll(elemRoot())
    }

    protected fun sentRefreshAllListener() {
        listenerList.forEach {it.refreshAll(elemRoot())}
    }

    private fun readData() {
        synchronized(dataList) { dataList.clear() }

        beforeRead()

        orm.select(clazz(), ::callBackSelectData)

        sentRefreshAllListener()
    }

    @Throws(SessionException::class)
    fun save(item :T, sessionSetting : SessionSetting = SessionSetting(false)) :T {

        val type = orm.save(item, sessionSetting)

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

    fun startLongTransation(): SessionSetting = orm.startLongTransation()

    fun commitLongTransaction(sessionSetting: SessionSetting) {
        orm.commitLongTransaction(sessionSetting)
    }

    fun rollbackLongTransaction(sessionSetting: SessionSetting) {
        orm.rollbackLongTransaction(sessionSetting)
    }
}

