package ru.barabo.babloz.db.service.budget

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreService

object BudgetCategoryService: StoreService<Category, GroupCategory>(BablozOrm) {

    private var rootBudgetCategory: GroupCategory? = null

    private lateinit var lastParent: GroupCategory

    public override fun elemRoot(): GroupCategory = rootBudgetCategory!!

    override fun clazz(): Class<Category> = Category::class.java

    private const val SELECT_SELECTED_ONLY =
            """select coalesce( (select 1 from BUDGET_CATEGORY bc where bc.BUDGET_ROW = ? and bc.category = c.id), 0) selected,
                      c.*
                from CATEGORY c
               where c.type = 0 and
                c.id in
                    (select allc.id
                      from BUDGET_CATEGORY bc,
                           CATEGORY cz,
                           CATEGORY allc
                     where bc.BUDGET_ROW = ?
                       and cz.id = bc.category
                       and ( allc.id in (cz.parent, cz.id) or (allc.parent = coalesce(cz.parent, cz.id) ) )
                    )
            order by case when c.parent is null then 100000*c.id else 100000*c.parent + c.id end"""

    private const val SELECT_OTHERS_ONLY =
            """select 1 selected, c.*
                from CATEGORY c
                where c.type = 0 and
                    c.id not in (select allc.id
                      from BUDGET_CATEGORY bc,
                           CATEGORY cz,
                           CATEGORY allc
                     where bc.BUDGET_ROW = ?
                       and cz.id = bc.category
                       and ( allc.id in (cz.parent, cz.id) or (allc.parent = coalesce(cz.parent, cz.id) ) )
                    )
            order by case when c.parent is null then 100000*c.id else 100000*c.parent + c.id end"""

    override fun initData() {

        dataList.clear()

        beforeRead()

        val params: Array<Any?>? = arrayOf(BudgetRow.budgetRowSelected?.id)

        orm.select(selectCategories(), params, clazz(), ::callBackSelectData)

        sentRefreshAllListener(EditType.INIT)
    }

    private fun selectCategories(): String =
            if(BudgetRow.budgetRowSelected?.isOther() == true) SELECT_OTHERS_ONLY else SELECT_SELECTED_ONLY

    override fun beforeRead() {

        rootBudgetCategory?.child?.clear() ?: run{rootBudgetCategory = GroupCategory()}

        lastParent = rootBudgetCategory!!
    }

    override fun processInsert(item: Category) {

        GroupCategory.addCategory(item, rootBudgetCategory!!, lastParent, {lastParent = it})
    }
}