package ru.barabo.babloz.db.service.filter

import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.entity.Project
import ru.barabo.db.service.FilterStore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface FilterPay : FilterStore<Pay> {

    val accountsFilter: MutableList<Int>

    val categoryFilter: MutableList<Int>

    val projectFilter: MutableList<Int>

    var dateStart: LocalDateTime

    var dateEnd: LocalDateTime

    fun setProjectFilter(projects: List<Project>) {
        val newIdProjects = projects.filter { it.id != null }.map { it.id!! }

        projectFilter.setNewFilter(newIdProjects)
    }

    fun setAccountFilter(accounts: List<Account>) {
        val newIdAccounts = accounts.filter { it.id != null }.map { it.id!! }

        accountsFilter.setNewFilter(newIdAccounts)
    }

    fun setDateFilter(start: LocalDate, end: LocalDate) {

        dateStart = start.atStartOfDay()

        dateEnd = end.atTime(LocalTime.MAX)

        setAllFilters()
    }

    fun setCategoryFilter(categories: List<Category>) {

        val newIdCategories = categories.filter { it.id != null }.map { it.id!! }

        categoryFilter.setNewFilter(newIdCategories)
    }

    private fun MutableList<Int>.setNewFilter(newFilter: List<Int>) {
        clear()
        addAll(newFilter)

        setAllFilters()
    }

    private fun MutableList<Int>.isAccessAccount(pay: Pay) =
        isEmpty() || (pay.account?.id in this) || (pay.accountTo?.id in this)


    private fun MutableList<Int>.isAccessCategory(pay: Pay) = isEmpty() || (pay.category?.id in this)

    private fun MutableList<Int>.isAccessProject(pay: Pay) = isEmpty() || (pay.project?.id in this)

    private fun setAllFilters() {

        getDataListStore().clear()

        if(allData?.isEmpty() != false) return

        for (pay in allData!!) {

            if(!accountsFilter.isAccessAccount(pay)) continue

            if(!categoryFilter.isAccessCategory(pay)) continue

            if(!projectFilter.isAccessProject(pay)) continue

            if(!filterCriteria.isAccess(pay) ) continue

            if(!pay.inDateRange()) continue

            getDataListStore().add(pay)
        }

        afterFilterAction()
    }

    fun Pay.inDateRange(): Boolean {
        if(dateStart.toLocalDate() == LocalDate.MIN && dateEnd.toLocalDate() == LocalDate.MIN) return true

        if(created == null) return false

        return (dateStart <= created!!) && (created!! <= dateEnd)
    }

    fun afterFilterAction()

    override fun setCriteria(criteria: String) {

        super.setCriteria(criteria)

        setAllFilters()
    }
}