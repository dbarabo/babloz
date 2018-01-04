package ru.barabo.db.converter

import ru.barabo.db.ConverterValue

object EnumConverter : ConverterValue {

    @Suppress("UNCHECKED_CAST")
    override fun convertFromBase(value: Any, javaType: Class<*>): Any? {
        val valNumber = (value as Number).toInt()

        val enums = javaType.enumConstants as Array<out Enum<*>>

        return enums[valNumber]
    }

    override fun convertToBase(value :Any) :Any = (value as Enum<*>).ordinal
}