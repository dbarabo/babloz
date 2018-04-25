package ru.barabo.babloz.gui.binding

import javafx.beans.binding.BooleanBinding

interface Saver<T> {

    fun isDisableEdit(): BooleanBinding

    fun save()

    fun cancel()

    fun changeSelectEditValue(value: T?)
}