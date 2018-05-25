package ru.barabo.db.service

import ru.barabo.db.EditType

interface StoreListener<T> {

    fun refreshAll(elemRoot: T, refreshType: EditType)
}