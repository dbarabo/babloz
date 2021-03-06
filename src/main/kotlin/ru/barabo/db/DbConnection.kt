package ru.barabo.db

import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.Connection.TRANSACTION_READ_COMMITTED
import java.sql.SQLException

open class DbConnection(private val dbSetting: DbSetting) {
    private val logger = LoggerFactory.getLogger(DbConnection::class.java)

    companion object {

        private const val TRY_CONNECT_MAX = 1

        private const val ERROR_TRY_MAX_CONNECT = "Кол-во попыток подключений превысило $TRY_CONNECT_MAX"
    }

    private val pool = ArrayList<Session>()

    init {
        Class.forName(dbSetting.driver)//.newInstance()
    }

    @Throws(SessionException::class)
    fun getSession(sessionSetting: SessionSetting): Session {

        return getTrySession(0, sessionSetting.isReadTransact, sessionSetting.transactType, sessionSetting.idSession)
    }

    fun closeSession(session: Session) {
        synchronized(pool) { pool.remove(session) }

        try {
            session.session.close()
        } catch (e :SQLException) {
            logger.error("closeDeathConnect", e)
        }
    }

    @Synchronized
    fun closeAllSessions() {
        pool.forEach { it.killSession() }

        synchronized(pool) { pool.clear() }
    }

    private fun closeDeathSessions() {
        val deathList = synchronized(pool) { pool.filter { !it.checkConnect(dbSetting.selectCheck) } }

        deathList.forEach { closeSession(it) }
    }

    private fun isDeathSession(session :Session) :Boolean {
        closeDeathSessions()

        synchronized(pool){
            return !pool.contains(session)
        }
    }

    private fun isRestartByNewSession(session :Session, isRead :Boolean) :Boolean {

        val newSession = getTrySession(0, isRead, TransactType.COMMIT, session.idSession)

        session.session = newSession.session

        synchronized(pool) {
            pool.remove(newSession)
            pool.add(session)
            return true
        }
    }

    fun isRestartSessionException(session :Session, isRead :Boolean, exceptionMessage: String) :Boolean {
        if( isDeathSession(session) ) {
            return isRestartByNewSession(session, isRead)
        }

        if(isPacketConnectError(exceptionMessage) ) {
            closeSession(session)
            return isRestartByNewSession(session, isRead)
        }

        return false
    }

    private fun isPacketConnectError(exceptionMessage: String)  = exceptionMessage.indexOf("ORA-04061") >= 0

    @Throws(SessionException::class)
    private fun getTrySession(tryCount :Int, isRead :Boolean, transactType :TransactType, idSession :Long?) :Session {

        val newTryCount = if(tryCount > TRY_CONNECT_MAX) throw SessionException(ERROR_TRY_MAX_CONNECT) else tryCount + 1

        val isReadTransact = if (transactType === TransactType.SET_SAVEPOINT_BEFORE ||
                             transactType === TransactType.ROLLBACK_SAVEPOINT) false else isRead

        var session = if(idSession != null) getSessionById(idSession, isReadTransact) else getFreeSession(isReadTransact)

        if(session == null) {
            session = addSession(isRead, idSession)
        }

        if(isDeathSession(session)) {

            return getTrySession(newTryCount, isReadTransact, transactType, idSession)
        }

        session.isFree = false

        return session
    }

    protected fun checkBase(session :Session? = null) :Boolean {

        return try {
            var sessionCheck = session

            if(sessionCheck == null) {
                sessionCheck = addSession(false)
            }

            val result = sessionCheck.checkConnect(dbSetting.selectCheck)

            if(session == null) {
                sessionCheck.isFree = true
            }

            result
        } catch (e :SessionException) {
            false
        }
    }

    @Throws(SessionException::class)
    protected fun addSession(isRead :Boolean, idSession :Long? = null) :Session {

        val connect = try {
            logger.info("dbSetting.url=${dbSetting.url} dbSetting.user=${dbSetting.user} dbSetting.password=${dbSetting.password}")
            java.sql.DriverManager.getConnection(dbSetting.url, dbSetting.user, dbSetting.password)

        } catch (e :SQLException) {
            logger.error("dbSetting.url=${dbSetting.url} dbSetting.user=${dbSetting.user} dbSetting.password=${dbSetting.password}")
            logger.error("addSession", e)
            throw SessionException(e.message as String)
        }

        try {
            connect.autoCommit = false

            connect.transactionIsolation = if(dbSetting.driver.contains("sqlite")) Connection.TRANSACTION_SERIALIZABLE
               else TRANSACTION_READ_COMMITTED

            connect.isReadOnly = isRead

        } catch (e :SQLException) {

            logger.error("addSession", e)
            try { connect.close() } catch (e2 :SQLException){}
            throw SessionException(e.message as String)
        }

        val session = Session(connect, false, idSession)

        synchronized(pool) {
            pool.add(session)
        }

        logger.error("CONNECT IS CREATE ${dbSetting.url} isReadOnly=$isRead, idSession=$idSession")
        return session
    }

    private fun getFreeSession(isReadTransact :Boolean) :Session? {

        synchronized(pool) {
            val thread = Thread.currentThread()

            return pool.firstOrNull {
                it.isFree &&
                it.idSession == null &&
                it.session.isReadOnly == isReadTransact &&
                it.thread == thread
            }
        }
    }

    @Throws(SessionException::class)
    private fun getSessionById(idSessionFind: Long, isReadTransact :Boolean) :Session? {

        synchronized(pool) {
            val thread = Thread.currentThread()

            val session = pool.firstOrNull {it.idSession == idSessionFind && it.thread == thread}

            return session?.let { it } ?:
                    getFreeSession(isReadTransact)?.apply { synchronized(this) {this.idSession = idSessionFind} }
        }
    }
}