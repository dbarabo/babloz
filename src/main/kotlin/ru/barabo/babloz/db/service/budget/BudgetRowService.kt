package ru.barabo.babloz.db.service.budget

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.db.SessionException
import ru.barabo.db.SessionSetting
import ru.barabo.db.service.StoreService

object BudgetRowService: StoreService<BudgetRow, List<BudgetRow> >(BablozOrm, BudgetRow::class.java) {

    public override fun elemRoot(): List<BudgetRow> = dataList

    override fun save(item: BudgetRow, sessionSetting: SessionSetting): BudgetRow {
        val result =  super.save(item, sessionSetting)

        if(item.isOther()) return result

        val otherItem = findOtherItem()?: return result

        otherItem.id?.let { orm.reCalcValue(it, otherItem, sessionSetting) }

        BudgetMainService.reCalcSelectedRow()

        return result
    }

    private fun findOtherItem(): BudgetRow? = dataList.firstOrNull { it.isOther() }

    @Throws(SessionException::class)
    override fun delete(item: BudgetRow, sessionSetting: SessionSetting) {

        if(item.isOther()) {
            throw SessionException(ERROR_DEL_OTHER)
        }
        super.delete(item, sessionSetting)
    }

    private const val ERROR_DEL_OTHER = "Нельзя удалять категорию <Все остальные категории>"

}