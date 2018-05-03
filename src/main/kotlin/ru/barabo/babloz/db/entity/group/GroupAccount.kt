package ru.barabo.babloz.db.entity.group

import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.AccountType
import ru.barabo.babloz.db.service.AccountService
import tornadofx.observable
import java.text.DecimalFormat

data class GroupAccount(var account: Account = Account(),
                        var parent : GroupAccount? = null,
                        val child: MutableList<GroupAccount> = ArrayList<GroupAccount>().observable()
                        ) {

    companion object {
        val root = GroupAccount()

        private val current = GroupAccount(Account(name = AccountType.CURRENT.label, type = AccountType.CURRENT), parent = root)

        private val credit = GroupAccount(Account(name = AccountType.CREDIT.label, type = AccountType.CREDIT), parent = root)

        private val deposit = GroupAccount(Account(name = AccountType.DEPOSIT.label, type = AccountType.DEPOSIT), parent = root)

        init {
            root.child.add(current)

            root.child.add(credit)

            root.child.add(deposit)
        }

        fun rootClear() {
            synchronized(current.child) { current.child.clear() }

            synchronized(credit.child) { credit.child.clear() }

            synchronized(deposit.child) { deposit.child.clear() }
        }

        private fun groupAccountByType(type: AccountType) = when (type) {
            AccountType.CURRENT -> current
            AccountType.CREDIT -> credit
            AccountType.DEPOSIT -> deposit
        }

        fun addAccount(account: Account): GroupAccount {

            val parent = groupAccountByType(account.type!!)

            val group = GroupAccount(account, parent)

            synchronized(parent.child) { parent.child.add(group) }

            return group
        }

        fun findByAccountName(accountName: String): GroupAccount? {

            if(root.child[0].child.size == 0) {
                AccountService.NULL_ACCOUNT
            }

            return GroupAccount.root.findByAccountName(accountName)
        }
    }

    private fun findByAccountName(accountName: String): GroupAccount? {
        if(accountName == account.name) return this

        for (group in child) {
            val find = group.findByAccountName(accountName)

            if(find != null) {
                return find
            }
        }
        return null
    }

    val name: String get() = account.name?.let { it } ?: ""

    val rest: String get() = account.rest?.let { DecimalFormat("0.00").format(it) + " ${account.currency?.ext}" }?:""
}