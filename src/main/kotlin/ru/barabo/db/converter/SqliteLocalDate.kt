package ru.barabo.db.converter

import ru.barabo.db.ConverterValue
import ru.barabo.db.Type
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


object SqliteLocalDate : ConverterValue {
    override fun convertFromBase(value: Any, javaType: Class<*>): Any? {

        val milliseconds = when {
            value is Number -> value.toLong()
            value is Date -> value.time
            else -> throw Exception("unknown class of value $value")
        }

        return when(javaType) {
            java.time.LocalDateTime::class.javaObjectType -> Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime()

            java.time.LocalDate::class.javaObjectType -> Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDate()

            else -> throw Exception("unknown class of javaType $javaType")
        }
    }

    override fun convertToBase(value :Any) :Any = if(value is LocalDate)
        Type.Companion.localDateToSqlDate(value) else
        Type.Companion.localDateToSqlDate(value as LocalDateTime)
}