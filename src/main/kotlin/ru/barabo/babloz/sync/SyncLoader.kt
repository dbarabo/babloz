package ru.barabo.babloz.sync

import ru.barabo.archive.Archive
import ru.barabo.babloz.db.service.*
import ru.barabo.babloz.db.service.budget.BudgetCategoryService
import ru.barabo.babloz.db.service.budget.BudgetMainService
import ru.barabo.babloz.db.service.budget.BudgetRowService
import ru.barabo.db.service.StoreService
import ru.barabo.db.sync.PREFIX_TABLE
import ru.barabo.db.sync.splitLines
import java.io.File

object SyncLoader {

    //private val logger = LoggerFactory.getLogger(SyncLoader::class.java)

    /**
     * return true, if exists update new or delete data for sync else false
     */
    fun loadSyncBackup(backupZip: File): Boolean {
        val services =
                serviceHashByTableName.values.map { Pair(it.clazz, it.prepareFillNewData()) }.toMap()

        val isExistUpdate =
                serviceHashByTableName.values.firstOrNull { it.isExistsUpdateSyncData() }

        fromZipBackup(backupZip)

        serviceHashByTableName.values.forEach { it.linkNewIdReference(services) }

        serviceHashByTableName.values.reversed().forEach { it.clearAllDataDb() }

        serviceHashByTableName.values.forEach { it.saveDataToDb() }

        serviceHashByTableName.values.forEach { it.initData() }

        return isExistUpdate != null
    }

    fun isExistsUpdateSyncData()
            = serviceHashByTableName.values.firstOrNull { it.isExistsUpdateSyncData() } != null

    private fun fromZipBackup(backupZip: File) {
        val text = Archive.unpackFromZipToString(backupZip)

        val lines = text.lines()

        var positionLine = 0

        while (positionLine < lines.size) {
            positionLine = findService(lines, positionLine)
        }
    }

    private fun findService(lines: List<String>, positionLine: Int): Int {

        val servicePair = findServiceOnly(lines, positionLine) ?: return lines.size

        val posSelected = servicePair.second

        val columnNames = splitLines( lines[posSelected] )

        return columnNames?.let {
            servicePair.first?.loadBackup(lines, it, posSelected + 1)
        } ?: findService(lines, posSelected)
    }

    private fun findServiceOnly(lines: List<String>, positionLine: Int): Pair<StoreService<*, *>?, Int>? {
        var posSelected = positionLine

        var line: String = lines[posSelected]

        while (posSelected < lines.size && line.indexOf(PREFIX_TABLE) != 0) {

            posSelected++
            line = lines[posSelected]
        }

        if (posSelected == lines.size) return null

        posSelected++

        val service = getService(line.substringAfter(PREFIX_TABLE).trim().toUpperCase())
                ?: return null

        return Pair(service, posSelected)
    }

    private fun getService(serviceName: String): StoreService<*, *>? = serviceHashByTableName[serviceName]

    internal val serviceHashByTableName = mapOf<String, StoreService<*, *>>(
            CurrencyService.tableName.toUpperCase() to CurrencyService,
            AccountService.tableName.toUpperCase() to  AccountService,
            CategoryService.tableName.toUpperCase() to  CategoryService,
            PersonService.tableName.toUpperCase() to  PersonService,
            ProjectService.tableName.toUpperCase() to  ProjectService,
            PayService.tableName.toUpperCase() to  PayService,
            BudgetRowService.tableName.toUpperCase() to  BudgetRowService,
            BudgetCategoryService.tableName.toUpperCase() to  BudgetCategoryService,
            BudgetMainService.tableName.toUpperCase() to  BudgetMainService
    )
}

