package ru.barabo.db.sync

import org.slf4j.LoggerFactory
import ru.barabo.db.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.javaType

class SyncReload<T: Any>(private val orm : TemplateQuery, private val entityClass: Class<T>) : Sync<T> {

    private val logger = LoggerFactory.getLogger(SyncReload::class.java)

    private var insertData: List<T> = emptyList()

    private var deleteData: List<T> = emptyList()

    private var updateData: List<T> = emptyList()

    private var isPrepareFillNewData: Boolean = false

    private val backupData = ArrayList<T>()

    private val idMember = getIdMember(entityClass)

    private val columnsTable = getBackupColumnsTable(entityClass)

    override val tableName = getTableName(entityClass)

    private val columnsAnnotation = getColumnsAnnotation(entityClass)

    /**
     * Порядок действий
     *
     * 1. prepareFillNewData - наполняем листы новыми данными (кот. нет в бекапе) insertData, updateData, deleteData : ПО ВСЕМ СЕРВИСАМ в ЛЮБОМ ПОРЯДКЕ allServices.forEach{ it.prepareFillNewData }
     * 2. loadBackup - подгужаем бэкап в лист backupData, сразу удаляя записи из deleteData(новые удаленные) и меняя на updateData по ID : ТУТ ПОДГРУЖАЮТСЯ ДАННЫЕ ВСЕХ СЕРВИСОВ allServices.loadBackup
     * 3. linkNewIdReference - связываем ссылки на новые элементы с Entity этих элементов. Так как у новых элементов будут менятся ID, то другие таблицы ссылающ на эти эл-ты будут ссылаться уже на новые ID
     *                           ВАЖЕН ПОРЯДОК Сервисов - от Независимых к зависимым т.е. от CURRENCY к PAY allServices.sort().forEach{ it.prepareFillNewData }
     * 4. clearAllDataDb - Удаляем все данные из DB. ВАЖЕН ПОРЯДОК Сервисов - от зависимых к Независимым т.е. от PAY к CURRENCY allServices.sortDesc().forEach{ it.DELETE_ALL }
     * 5. saveDataToDb - меняет новые ID, синхронизируя их backupData. Тут же загружает данные в DB. Сначала старые backupData, потом новые insertData
     *                   ВАЖЕН ПОРЯДОК Сервисов - от Независимых к зависимым т.е. т.е. от CURRENCY к PAY allServices.sort().forEach{ it.INSERT_DATA }
     */



    override fun isExistsUpdateSyncData(): Boolean =
            if(isPrepareFillNewData) isExistsFilledNewData() else checkNewDataByQuery()

    private fun isExistsFilledNewData() = insertData.isNotEmpty() || updateData.isNotEmpty() || deleteData.isNotEmpty()

    override fun clearAllDataDb() {

        isPrepareFillNewData = false

        val deleteAll = templateDeleteAllSql(tableName)

        orm.executeQuery(deleteAll, null)
    }

    private fun checkNewDataByQuery(): Boolean {

        val syncColumn = getTransientColumns(entityClass).firstOrNull() ?: return false

        val query = selectTableWhereTemplate("COUNT(*)", tableName, "$syncColumn not null")

        val isExists = orm.selectValue(query) as? Number

        return isExists?.toInt() ?: 0 != 0
    }

    override fun prepareFillNewData(): List<T> {
        val selectNewRecords = getSelectNewRecords()

        insertData = selectNewRecords?.let { getSelectNewData(it, SyncEditTypes.INSERT) } ?: emptyList()

        insertData = insertData.sortedWith( compareBy { idMember!!.getter.call(it) as Comparable<*> } )

        deleteData = selectNewRecords?.let { getSelectNewData(it, SyncEditTypes.DELETE) } ?: emptyList()

        updateData = selectNewRecords?.let { getSelectNewData(it, SyncEditTypes.UPDATE) } ?: emptyList()

//        logger.error("insertData=$insertData")
//        logger.error("deleteData=$deleteData")
//        logger.error("updateData=$updateData")

        isPrepareFillNewData = true

        return insertData
    }

    override fun saveDataToDb() {

        updateNewId()

        saveAllData()
    }

    /**
     * return last processed positionLine
     */
    override fun loadBackup(lines: List<String>, columns: List<String>, positionLine: Int): Int {

        val resultPosition = loadBackupData(lines, columns, positionLine)

        glueWithNewData()

        return resultPosition
    }

    override fun linkNewIdReference(services: Map<Class<*>, List<Any>? >) {

        if(!isExistsNewUpdateData()) return

        val memberEntityFields = getMemberEntityFields(entityClass) ?: return

        for (memberEntityField in memberEntityFields) {
            val type = memberEntityField.returnType.javaType as Class<*>

            val idMemberEntityField = getIdMember(type) ?: continue

            val newListIdEntity = services[type] ?: continue

            updateNewFieldsReference(memberEntityField, newListIdEntity, idMemberEntityField)
        }
    }

    private fun saveAllData() {

        if(backupData.isEmpty() && insertData.isEmpty()) return

        val memberColumns = getColumnsInsertAnnotation(entityClass)

        val insertSql = getInsertSql(memberColumns.keys)

        val session = orm.startLongTransation()

        try {
            saveData(insertSql, memberColumns.values, backupData, session)

            saveData(insertSql, memberColumns.values, insertData, session)
        } catch (e: Exception) {
            orm.rollbackLongTransaction(session)

            logger.error("saveAllData", e)

            throw Exception(e.message)
        }
        orm.commitLongTransaction(session)
    }

    private fun saveData(query: String, memberColumns: Collection<MemberConverter>, data: List<T>, session: SessionSetting) {

        for (item in data) {
            val params = getSqlParamsFromEntity(item, memberColumns)

            orm.executeQuery(query, params, session)
        }
    }

    private fun getInsertSql(columns: Set<String>): String {
        val columnNames = columns.joinToString(", ")

        val questions = columns.joinToString(", ") { "?" }

        return templateInsertSql(tableName, columnNames, questions)
    }

    private fun templateDeleteAllSql(table: String) = "delete from $table"

    private fun templateInsertSql(table: String, columns: String, questions: String) = "insert into $table ( $columns ) values ( $questions )"

    private fun updateNewId() {
        if(insertData.isEmpty()) return

        val maxItem = backupData.maxWith( compareBy { idMember!!.getter.call(it) as? Comparable<*> } ) ?: return

        val maxId = idMember!!.getter.call(maxItem) ?: return

        val clazz = idMember.returnType.javaType as Class<*>

        var nextId = Type.increment(clazz, maxId)

        for(newItem in insertData) {

            idMember.setter.call(newItem, nextId)

            nextId = Type.increment(clazz, nextId)
        }
    }

    private fun isExistsNewUpdateData() = insertData.isNotEmpty() || updateData.isNotEmpty()

    private fun updateNewFieldsReference(memberEntityField: KMutableProperty<*>, newListIdEntity: List<Any>,
                                idMemberEntityField: KMutableProperty<*>) {

        updateNewFields(memberEntityField, newListIdEntity, idMemberEntityField, insertData)

        updateNewFields(memberEntityField, newListIdEntity, idMemberEntityField, updateData)
    }

    private fun updateNewFields(memberEntityField: KMutableProperty<*>, newListIdEntity: List<Any>,
                                idMemberEntityField: KMutableProperty<*>, newDataList: List<T> ) {

        for (insItem in newDataList) {
            val oldEntity = memberEntityField.getter.call(insItem) ?: continue

            val id = idMemberEntityField.getter.call(oldEntity) ?: continue

            val newEntity = newListIdEntity.firstOrNull { idMemberEntityField.getter.call(it) == id } ?: continue

            memberEntityField.setter.call(insItem, newEntity)
        }
    }

    private fun glueWithNewData() {

        deleteRecordFromBackup(backupData, deleteData, idMember)

        updateRecordFromBackup(backupData, updateData, idMember)
    }

    private fun deleteRecordFromBackup(backupData: MutableList<T>, deleteData: List<T>, idMember: KMutableProperty<*>?) {

        for (delItem in deleteData) {

            val delId = idMember?.getter?.call(delItem) ?: continue

            val item = backupData.firstOrNull { idMember.getter.call(it) == delId }

            item?.let { backupData.remove(it) }
        }
    }

    private fun updateRecordFromBackup(backupData: MutableList<T>, updateData: List<T>, idMember: KMutableProperty<*>?) {

        for (updateItem in updateData) {

            val updateId = idMember?.getter?.call(updateItem) ?: continue

            val item = backupData.firstOrNull { idMember.getter.call(it) == updateId }

            item?.let {
                backupData.remove(it)
                backupData += updateItem
            }
        }
    }

    private fun loadBackupData(lines: List<String>, columns: List<String>, positionLine: Int): Int {

        backupData.clear()

        var posSelected = positionLine

        while (posSelected < lines.size && lines[posSelected].indexOf(PREFIX_TABLE) != 0) {

            val values = splitLines(lines[posSelected])

            values?.let { backupData += getEntityFromString(entityClass.newInstance(), columnsAnnotation, it, columns) }

            posSelected++
        }

        return posSelected
    }

    private fun getSelectNewData(selectRecords: Triple<String, Int, List<String>>, syncType: SyncEditTypes): List<T> {

        val params: Array<Any?>? = Array(selectRecords.second)  { syncType.ordinal }

        val data =  orm.select(selectRecords.first, params)

        return mapDataToEntities(data, selectRecords.third)
    }

    private fun mapDataToEntities(data: List<Array<Any?>>, columns: List<String>): List<T> {

        val dataList = ArrayList<T>()

        for (row in data) {

            dataList += getEntityFromSql(entityClass.newInstance(), columnsAnnotation, row, columns)
        }

        return dataList
    }

    private fun getSelectNewRecords(): Triple<String, Int, List<String>>? {

        val transient = getTransientColumns(entityClass)

        if(columnsTable.isEmpty() || transient.isEmpty()) return null

        val query = selectQuery(columnsTable, transient)

        return Triple(query, transient.size, columnsTable)
    }

    private fun selectQuery(columns: List<String>, whereColumns: List<String>) =
            selectTableWhereTemplate(columns.joinToString(), tableName,
                    whereColumns.joinToString(separator = " = ? and ", postfix = " = ?"))

    private fun selectTableWhereTemplate(columns: String, table: String, where: String) = "select $columns from $table where $where"

    override fun getBackupData(): String {
        if(columnsTable.isEmpty()) return ""

        val header = getBackupTableHeader(tableName, columnsTable)

        val data = selectTableRows(columnsTable)

        return header + data.joinToString("\n") { it.joinToString(COLUMN_SEPARATOR).replace("\n".toRegex(), "") }
    }

    override fun resetNewSyncData() {

        val transient = getTransientColumns(entityClass)

        if(transient.isEmpty()) return

        val query = updateSyncQuery(transient)

        orm.executeQuery(query, null)
    }

    private fun updateSyncQuery(syncColumns: List<String>) =
            updateSyncTemplate(tableName, syncColumns.joinToString(separator = " = null, ", postfix = " = null"))

    private fun updateSyncTemplate(table: String, syncColumns: String) = "update $table set $syncColumns"

    @Throws(SessionException::class)
    private fun selectTableRows(columns: List<String>): List<Array<Any?>> {

        val selectTable = selectTableTemplate( columns.joinToString(), tableName )

        return orm.select(selectTable)
    }

    private fun selectTableTemplate(columns: String, table :String) = "select $columns from $table"
}

const val PREFIX_TABLE = "@@@"

const val COLUMN_SEPARATOR = "\b"

fun splitLines(line: String): List<String>? {

    if(line.trim().isEmpty()) return null

    return line.split(COLUMN_SEPARATOR)
}

fun getBackupTableHeader(tableName: String,
                         columns: List<String>,
                         prefix: String = PREFIX_TABLE,
                         separColumns: String = COLUMN_SEPARATOR): String = "$prefix$tableName\n${columns.joinToString(separColumns)}\n"