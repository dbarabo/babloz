package ru.barabo.babloz.db

import org.slf4j.LoggerFactory
import ru.barabo.db.TemplateQuery
import ru.barabo.db.Type

object BablozOrm :TemplateQuery(BablozQuery)

val logger = LoggerFactory.getLogger(BablozOrm::class.java)

inline fun <reified T> selectValueType(select: String, params: Array<in Any?>? = null): T? {

    val result = BablozOrm.selectValue(select, params) ?: return null

    if(result is T) return result

    return Type.convertValueToJavaTypeByClass(result, T::class.java) as? T
}
