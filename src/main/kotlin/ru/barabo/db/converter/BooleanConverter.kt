package ru.barabo.db.converter

import ru.barabo.db.ConverterValue

object BooleanConverter : ConverterValue {
    override fun convertFromBase(value: Any, javaType: Class<*>): Any? = (value as? Number)?.toInt() != 0

    override fun convertFromStringToJava(value: String, javaType: Class<*>): Any? = value != "0"
}