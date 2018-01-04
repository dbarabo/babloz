package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.GroupAccount
import ru.barabo.db.service.StoreService

object AccountService :StoreService<Account, GroupAccount>(BablozOrm){

    override fun elemRoot(): GroupAccount = GroupAccount.root

    override fun clazz(): Class<Account> = Account::class.java

    override fun beforeRead() {
        GroupAccount.rootClear()
    }

    override fun processInsert(item: Account) {

        GroupAccount.addAccount(item)
    }
}
