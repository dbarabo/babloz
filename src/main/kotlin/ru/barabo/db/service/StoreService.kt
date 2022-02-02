package ru.barabo.db.service

import org.slf4j.LoggerFactory
import ru.barabo.db.*
import ru.barabo.db.sync.Sync
import ru.barabo.db.sync.SyncReload
import ru.barabo.db.sync.SyncEditTypes
import tornadofx.asObservable
import tornadofx.observable

abstract class StoreService<T: Any, out G>(protected val orm: TemplateQuery, val clazz: Class<T>)
    : Sync<T> by SyncReload<T>(orm, clazz) {

    private val listenerList = ArrayList<StoreListener<G>>()

    private val logger = LoggerFactory.getLogger(StoreService::class.java)

    protected val dataList = ArrayList<T>().asObservable()

    @Volatile
    private var startedLongTransaction: LongTransactState = LongTransactState.NONE_LONG_TRANSACT

    init {
        initData()
    }

    protected abstract fun elemRoot(): G

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

    protected open fun sentRefreshAllListener(refreshType: EditType) {
        listenerList.forEach { it.refreshAll(elemRoot(), refreshType) }
    }

    open fun initData() {
        dataList.removeAll(dataList)

        beforeRead()

        orm.select(clazz, ::callBackSelectData)

        sentRefreshAllListener(EditType.INIT)
    }

    @Throws(SessionException::class)
    open fun delete(item: T, sessionSetting: SessionSetting = SessionSetting(false)) {

        dataList.remove(item)

        if(setDeleteSyncValue(item)) {
            orm.save(item, sessionSetting)
        } else {
            orm.deleteById(item, sessionSetting)
        }

        processDelete(item)

        processStartLongTransactState(EditType.DELETE)
    }

    fun reCalcItemById(idParam: Any, item: T, sessionSetting: SessionSetting = SessionSetting(false)) {

        orm.reCalcValue(idParam, item, sessionSetting)
    }

    @Throws(SessionException::class)
    open fun save(item: T, sessionSetting: SessionSetting = SessionSetting(false)): T {

        setSaveSyncValue(item)
//        logger.error("save item=$item")

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

    private fun setDeleteSyncValue(item: T) = setSyncValue(item, SyncEditTypes.DELETE.ordinal)

    private fun setSaveSyncValue(item: T) {

        val syncValue = if(isNullIdItem(item)) SyncEditTypes.INSERT else SyncEditTypes.UPDATE

//        logger.error("syncValue=$syncValue")
//        logger.error("syncValue.ordinal=${syncValue.ordinal}")

        setSyncValue(item, syncValue.ordinal)
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

        return orm.startLongTransaction()
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

