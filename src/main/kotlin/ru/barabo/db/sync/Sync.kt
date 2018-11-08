package ru.barabo.db.sync

interface Sync<T> {
    /**
     * fill new data from service
     * @return new data list
     */
    fun prepareFillNewData(): List<T>

    /**
     * load backup data from lines start with positionLine. columns - column names data in lines
     * @return next after processing position in lines
     */
    fun loadBackup(lines: List<String>, columns: List<String>, positionLine: Int): Int

    /**
     * link new values with new list in map
     * after update id values in new values reference in other tables is updated too
     * @services - map all services in DB with new values
     */
    fun linkNewIdReference(services: Map<Class<*>, List<Any>? >)

    /**
     *  remove all data from table - before save sync data
     */
    fun clearAllDataDb()

    /**
     * insert new data to Db with update new id values
     */
    fun saveDataToDb()

    /**
     * return backup service table
     */
    fun getBackupData(): String

    /**
     * reset new sync data after sent backup
     */
    fun resetNewSyncData()

    /**
     * table name of service
     */
    val tableName: String

    /**
     * check exists new data or update data or delete data for sync
     */
    fun isExistsUpdateSyncData(): Boolean
}