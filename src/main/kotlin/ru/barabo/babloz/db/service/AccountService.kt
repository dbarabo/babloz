package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.entity.group.GroupAccount
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.db.service.StoreService

object AccountService :StoreService<Account, GroupAccount>(BablozOrm, Account::class.java), StoreListener<List<Pay>> {

    init {
        PayService.addListener(this)
    }

    override fun elemRoot(): GroupAccount = GroupAccount.root

    override fun beforeRead() {
        GroupAccount.rootClear()
    }

    override fun processInsert(item: Account) {

        GroupAccount.addAccount(item)
    }

    fun accountList() = dataList

    val NULL_ACCOUNT = Account()

    fun accountNullList(): List<Account> {

        val result = ArrayList<Account>()

        result += NULL_ACCOUNT

        result.addAll(dataList)

        return result
    }

    val ALL_ACCOUNT = Account(name = "ВСЕ счета")

    fun accountAllList(): List<Account> {
        val result = ArrayList<Account>()

        result += ALL_ACCOUNT

        result.addAll(dataList)

        return result
    }

    override fun refreshAll(elemRoot: List<Pay>, refreshType: EditType) {

        if(refreshType.isEditable()) {
            initData()
        }
    }
}

