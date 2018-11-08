package ru.barabo.babloz.sync

enum class SyncTypes(val label: String) {

    SYNC_START_SAVE_LOCAL("Синхронизация на старте-завершении, Не удалять локальную БД"),

    SYNC_START_DEL_LOCAL("Синхронизация на старте-завершении, Удалять локальную БД"),

    NO_SYNC_LOCAL_ONLY("Только локальная БД - без синхронизации");

    override fun toString(): String = label;

    companion object {
        val DEFAULT_SYNC_TYPE = SyncTypes.values()[0]
    }
}