package ru.barabo.babloz.db.service.report.restaccount

import ru.barabo.babloz.db.entity.Account

enum class AccountViewType(val label: String, val filteredList: (List<Account>) -> List<Account>) {

    BALANCE("Общий баланс", { listOf(Account(name = "Общий баланс")) } ),

    ALL("Все счета и типы", { list->

        val types = list.distinctBy { it.type }
                .map { Account(type = it.type, name = it.type?.label) }.toMutableList()

        types.apply { addAll(list) }
    }),

    TYPE_ONLY("Только типы счетов", { list->
        list.distinctBy { it.type }.map { Account(type = it.type, name = it.type?.label) } }),

    ACCOUNT_ONLY("Только счета", { it.filter { it.id != null  } } );

    override fun toString(): String = label
}