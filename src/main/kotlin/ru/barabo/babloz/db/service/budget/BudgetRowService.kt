package ru.barabo.babloz.db.service.budget

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.db.SessionSetting
import ru.barabo.db.service.StoreService

object BudgetRowService: StoreService<BudgetRow, List<BudgetRow> >(BablozOrm) {

    override fun clazz(): Class<BudgetRow> = BudgetRow::class.java

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


}