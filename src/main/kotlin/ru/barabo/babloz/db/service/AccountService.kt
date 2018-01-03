package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.GroupAccount

object AccountService {

    private val listenerList = ArrayList<StoreListener<GroupAccount>>()

    private val accountList = ArrayList<Account>()

    init {
        readAccount()
    }

    fun addListener(listener :StoreListener<GroupAccount>) {
        listenerList.add(listener)

        listener.refreshAll(GroupAccount.root)
    }

    private fun sentRefreshAllListener() {
        listenerList.forEach {it.refreshAll(GroupAccount.root)}
    }

    private fun readAccount() {

        synchronized(accountList) { accountList.clear() }

        GroupAccount.rootClear()

        BablozOrm.select(Account::class.java, ::callBackSelectAccount)

        sentRefreshAllListener()
    }

    private fun callBackSelectAccount(account :Account) {

        synchronized(accountList) { accountList.add(account) }

        GroupAccount.addAccount(account)
    }
}