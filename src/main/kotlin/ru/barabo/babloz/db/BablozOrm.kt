package ru.barabo.babloz.db

import org.slf4j.LoggerFactory
import ru.barabo.db.SessionSetting
import ru.barabo.db.TemplateQuery
import ru.barabo.db.Type

object BablozOrm : TemplateQuery(BablozQuery)

val logger = LoggerFactory.getLogger(BablozOrm::class.java)

inline fun <reified T> selectValueType(select: String, params: Array<in Any?>? = null,
                                       sessionSetting: SessionSetting = SessionSetting(isReadTransact = false)): T? {

    val result = BablozOrm.selectValue(select, params, sessionSetting) ?: return null

    if(result is T) return result

    return Type.convertValueToJavaTypeByClass(result, T::class.java) as? T
}

fun processLongTransactionsKill(process: (sessionSetting: SessionSetting)->Unit) {
    val sessionSetting = BablozOrm.startLongTransaction()

    try {
        process(sessionSetting)

        BablozOrm.commitLongTransaction(sessionSetting, isKillSession = true)
    } catch (e: Exception) {
        logger.error("processLongTansactionsKill", e)
        BablozOrm.rollbackLongTransaction(sessionSetting, isKillSession = true)

        throw Exception(e)
    }
}

