package ru.barabo.babloz.gui.account

import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.gui.binding.AbstractSaver

internal object AccountSaver : AbstractSaver<Account, AccountBind>(AccountBind::class.java) {

    override fun serviceSave(value: Account) {
        AccountService.save(value)
    }
}