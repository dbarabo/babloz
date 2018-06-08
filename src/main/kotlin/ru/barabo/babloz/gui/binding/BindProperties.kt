package ru.barabo.babloz.gui.binding

import javafx.beans.binding.BooleanBinding

interface BindProperties<T> {

    var editValue: T?

    fun isInit() = editValue != null

    fun fromValue(value: T?)

    fun initValue(value: T?) {
        editValue = value

        fromValue(value)
    }

    fun toValue(value: T)

    fun saveToEditValue() :T {
        if(editValue == null) throw  Exception("editValue is null")

        toValue(editValue!!)

        return editValue!!
    }

    fun copyToProperties(destination: BindProperties<T>)

    fun isEqualsProperties(compare: BindProperties<T>): BooleanBinding

    fun isDisableEdit(compare: BindProperties<T>) = isEqualsProperties(compare)
}