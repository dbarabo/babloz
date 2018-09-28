package ru.barabo.babloz.sync

import ru.barabo.archive.Archive
import ru.barabo.babloz.db.service.*
import ru.barabo.babloz.db.service.budget.BudgetCategoryService
import ru.barabo.babloz.db.service.budget.BudgetMainService
import ru.barabo.babloz.db.service.budget.BudgetRowService

object SyncSaver {

    fun toZipBackup() = Archive.packToZipStream( inputStream = getBackupData().byteInputStream())

    private fun getBackupData(): String =
            CurrencyService.getBackupData() +
            AccountService.getBackupData() +
            CategoryService.getBackupData() +
            PersonService.getBackupData() +
            ProjectService.getBackupData() +
            PayService.getBackupData() +
            BudgetMainService.getBackupData() +
            BudgetRowService.getBackupData() +
            BudgetCategoryService.getBackupData()
}