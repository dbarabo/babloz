package ru.barabo.db.service

import ru.barabo.db.EditType
import ru.barabo.db.SessionException
import ru.barabo.db.SessionSetting
import ru.barabo.db.TemplateQuery
import tornadofx.observable

abstract class StoreService<T: Any, G>(protected val orm :TemplateQuery) {

    private val listenerList = ArrayList<StoreListener<G>>()

    protected val dataList = ArrayList<T>().observable()

    @Volatile
    private var startedLongTransaction: LongTransactState = LongTransactState.NONE_LONG_TRANSACT

    init {
        initData()
    }

    protected abstract fun elemRoot(): G

    protected abstract fun clazz(): Class<T>

    protected open fun processDelete(item: T) {}

    protected open fun processInsert(item: T) {}

    protected open fun processUpdate(item: T) {}

    protected open fun beforeRead() {}

    protected fun callBackSelectData(item: T) {

        synchronized(dataList) { dataList.add(item) }

        processInsert(item)
    }

    fun addListener(listener : StoreListener<G>) {
        listenerList.add(listener)

        listener.refreshAll(elemRoot(), EditType.INIT)
    }

    protected fun sentRefreshAllListener(refreshType: EditType) {
        listenerList.forEach { it.refreshAll(elemRoot(), refreshType) }
    }

    open fun initData() {
        dataList.removeAll(dataList)

        beforeRead()

        orm.select(clazz(), ::callBackSelectData)

        sentRefreshAllListener(EditType.INIT)
    }


    @Throws(SessionException::class)
    open fun delete(item: T, sessionSetting: SessionSetting = SessionSetting(false)) {

        dataList.remove(item)

        orm.deleteById(item, sessionSetting)

        processDelete(item)
    }

    fun reCalcItemById(idParam: Any, item: T, sessionSetting: SessionSetting = SessionSetting(false)) {

        orm.reCalcValue(idParam, item, sessionSetting)
    }

    @Throws(SessionException::class)
    open fun save(item: T, sessionSetting: SessionSetting = SessionSetting(false)): T {

        val type = orm.save(item, sessionSetting)

        when (type) {
            EditType.INSERT -> {
                dataList.add(item)

                processInsert(item)
            }
            EditType.EDIT -> {
                processUpdate(item)
            }
            else -> throw SessionException("EditType is not valid $type")
        }

        processStartLongTransactState(type)

        return item
    }

    private fun processStartLongTransactState(type: EditType) {
        if(startedLongTransaction != LongTransactState.NONE_LONG_TRANSACT) {
            startedLongTransaction = LongTransactState.LONG_TRANSACT_MUST_REFRESH
        } else {
            sentRefreshAllListener(type)
        }
    }

    fun startLongTransation(): SessionSetting {
        startedLongTransaction = LongTransactState.LONG_TRANSACT_STARTED

        return orm.startLongTransation()
    }

    fun commitLongTransaction(sessionSetting: SessionSetting) {

        orm.commitLongTransaction(sessionSetting)

        processEndLongTransactState()
    }

    fun rollbackLongTransaction(sessionSetting: SessionSetting) {
        orm.rollbackLongTransaction(sessionSetting)

        processEndLongTransactState()
    }

    private fun processEndLongTransactState() {

        if(startedLongTransaction == LongTransactState.LONG_TRANSACT_MUST_REFRESH) {
            startedLongTransaction = LongTransactState.NONE_LONG_TRANSACT
            sentRefreshAllListener(EditType.ALL)
        }
        startedLongTransaction = LongTransactState.NONE_LONG_TRANSACT
    }
}

private enum class LongTransactState {
    NONE_LONG_TRANSACT,
    LONG_TRANSACT_STARTED,
    LONG_TRANSACT_MUST_REFRESH
}

