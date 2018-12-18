package ru.barabo.babloz.db.service.report

interface ChangeEntity<T> {

    val entitySet: MutableSet<T>

    fun addEntity(entity: T) {
        entitySet += entity

        updateEntityInfo(entity)
    }

    fun removeEntity(entity: T) {
        entitySet -= entity

        updateEntityInfo(entity)
    }

    fun updateEntityInfo(entity: T)
}
