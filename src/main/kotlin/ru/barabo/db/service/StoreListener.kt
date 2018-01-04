package ru.barabo.db.service

interface StoreListener<T> {

    fun refreshAll(elemRoot :T)
}