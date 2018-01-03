package ru.barabo.babloz.db.service

interface StoreListener<T> {

    fun refreshAll(root :T)
}