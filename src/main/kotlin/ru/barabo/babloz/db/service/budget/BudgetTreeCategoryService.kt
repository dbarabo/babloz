package ru.barabo.babloz.db.service.budget

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.budget.BudgetCategory
import ru.barabo.babloz.db.entity.budget.BudgetRow
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreService

object BudgetTreeCategoryService: StoreService<Category, GroupCategory>(BablozOrm) {

    private val logger = LoggerFactory.getLogger(BudgetTreeCategoryService::class.java)

    private var rootBudgetCategory: GroupCategory? = null

    private lateinit var lastParent: GroupCategory

    public override fun elemRoot(): GroupCategory = rootBudgetCategory!!

    override fun clazz(): Class<Category> = Category::class.java

    override fun initData() {

        dataList.clear()

        beforeRead()

        val params: Array<Any?>? = arrayOf(BudgetRow.budgetRowSelected?.id, BudgetRow.budgetRowSelected?.id)

        orm.select(selectCategories(), params, clazz(), ::callBackSelectData)

        val advancedParams: Array<Any?> = arrayOf(BudgetRow.budgetRowSelected?.main)

        advancedSelect()?.let{ orm.select(it, advancedParams, clazz(), ::callBackSelectData) }

        sentRefreshAllListener(EditType.INIT)
    }

    override fun beforeRead() {

        rootBudgetCategory?.child?.clear() ?: run{rootBudgetCategory = GroupCategory()}

        lastParent = rootBudgetCategory!!
    }

    override fun processInsert(item: Category) {

        GroupCategory.addCategory(item, rootBudgetCategory!!, lastParent, {lastParent = it})
    }

    private fun advancedSelect(): String? =
            if(BudgetRow.budgetRowSelected?.isOther() != false) null else SELECT_NOT_SELECTED_ONLY

    private fun selectCategories(): String =
            if(BudgetRow.budgetRowSelected?.isOther() == true) SELECT_OTHERS_ONLY else SELECT_SELECTED_ONLY

    private const val SELECT_SELECTED_ONLY =
            """select coalesce( (select 1 from BUDGET_CATEGORY bc where bc.BUDGET_ROW = ? and bc.category = c.id), 0) IS_SELECTED,
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
            """select 1 IS_SELECTED,
               c.*
                from CATEGORY c
                where c.type = 0 and
                    c.id not in (select bc.category from BUDGET_CATEGORY bc
                     where bc.BUDGET_ROW in (select br.id from BUDGET_ROW br where br.main in (?, ?) )
                    )
            order by case when c.parent is null then 100000*c.id else 100000*c.parent + c.id end"""

    private const val SELECT_NOT_SELECTED_ONLY =
            """select 0 IS_SELECTED, c.*
                from CATEGORY c
                where c.type = 0 and
                c.id not in (select allc.id
                from BUDGET_CATEGORY bc,
                BUDGET_ROW br,
                CATEGORY cz,
                CATEGORY allc
                where bc.BUDGET_ROW = br.ID
                and br.MAIN = ?
                and cz.id = bc.category
                and ( allc.id in (cz.id) /*(cz.parent, cz.id) or (allc.parent = coalesce(cz.parent, cz.id) )*/  )
                )
                order by case when c.parent is null then 100000*c.id else 100000*c.parent + c.id end"""


    fun addCategory(groupCategory: GroupCategory) {

        val budgetCategory = BudgetCategoryService.findByCategory(groupCategory.category)

        if(budgetCategory?.budgetRow == BudgetRow.budgetRowSelected?.id) return

        budgetCategory?.budgetRow?.let { BudgetCategoryService.delete(budgetCategory) }

        val newBudgetCategory = BudgetCategory(budgetRow = BudgetRow.budgetRowSelected?.id, category = groupCategory.category.id)

        BudgetCategoryService.save(newBudgetCategory)

        checkUpdateNameBudgetRow()
    }

    fun removeCategory(groupCategory: GroupCategory) {

        val budgetCategory = BudgetCategoryService.findByCategory(groupCategory.category)

        budgetCategory?.budgetRow?.let { BudgetCategoryService.delete(budgetCategory) }

        checkUpdateNameBudgetRow()
    }

    private fun checkUpdateNameBudgetRow(): Boolean {

        val budgetRowSelected = BudgetRow.budgetRowSelected ?: return false

        if(budgetRowSelected.isOther()) return false

        return updateRowByCategories(budgetRowSelected)
    }

    private fun updateRowByCategories(budgetRowSelected: BudgetRow): Boolean {

        val params: Array<Any?> = arrayOf(budgetRowSelected.id)

        val categoryList = orm.select(SELECT_CATEGORY_NAME_ROW, params).map { it[0] }.joinToString(",")

        budgetRowSelected.name = categoryList

        BudgetRowService.save(budgetRowSelected)

        return true
    }

    private const val SELECT_CATEGORY_NAME_ROW =
            """select c.name
                 from BUDGET_CATEGORY bc,
                      CATEGORY c
                where c.id = bc.category
                  and bc.BUDGET_ROW = ?
                  and (c.parent is null or c.parent not in (select cc.id
                        from CATEGORY cc,
                             BUDGET_CATEGORY bc2
                       where cc.id = bc2.category
                         and cc.parent is null
                         and bc2.BUDGET_ROW = bc.BUDGET_ROW))
               order by case when c.parent is null then 10000*c.id else 100000*c.parent + c.id end"""
}