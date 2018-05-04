package ru.barabo.babloz.db.service.filter

import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.db.service.FilterStore

interface FilterPay : FilterStore<Pay> {

    val accountsFilter: MutableList<Int>

    fun setAccountFilter(accounts: List<Account>) {
        accountsFilter.clear()
        val newIdAccounts = accounts.filter { it.id != null }.map { it.id!! }
        accountsFilter.addAll(newIdAccounts)

        setAllFilters()
    }

    private fun MutableList<Int>.isAccessAccount(pay: Pay) =
        isEmpty() || (pay.account?.id in this) || (pay.accountTo?.id in this)

    private fun setAllFilters() {

        getDataListStore().clear()

        if(allData?.isEmpty() != false) return

        for (pay in allData!!) {

            if(!accountsFilter.isAccessAccount(pay)) continue

            if(!filterCriteria.isAccess(pay) ) continue

            getDataListStore().add(pay)
        }
    }

    override fun setCriteria(criteria: String) {

        super.setCriteria(criteria)

        setAllFilters()
    }
}