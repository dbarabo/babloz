package ru.barabo.babloz.db.service.filter

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.db.service.FilterStore

interface FilterPay : FilterStore<Pay> {

    val accountsFilter: MutableList<Int>

    fun setAccountFilter(accounts: List<Account>) {
        accountsFilter.clear()
        val newIdAccounts = accounts.filter { it.id != null }.map { it.id!! }
        accountsFilter.addAll(newIdAccounts)
        //accountsFilter.forEach { LoggerFactory.getLogger(FilterPay::class.java).info("accountsFilter=${it.hashCode()}") }

        setAllFilters()
    }

    private fun MutableList<Int>.isAccessAccount(pay: Pay) : Boolean {

        val isAccess = accountsFilter.isEmpty() ||
                pay.account?.id in accountsFilter ||
                pay.accountTo?.id in accountsFilter

//        if(pay.account?.id == 1 || pay.accountTo?.id == 1) {
//            LoggerFactory.getLogger(FilterPay::class.java)
//                    .info("pay.account=${pay.account?.hashCode()} pay.accountTo=${pay.accountTo?.hashCode()} isAccessAccount=$isAccess")
//        }

        return isAccess
    }

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