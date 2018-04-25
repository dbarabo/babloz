package ru.barabo.babloz.gui.account

import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.gui.binding.Saver
import ru.barabo.babloz.gui.custom.ChangeSelectEdit

internal object AccountSaver :Saver<Account, AccountBind> {

    override val editBind: AccountBind = AccountBind(null)

    override val oldBind: AccountBind = AccountBind(null)

    override var changeSelectEdit: ChangeSelectEdit = ChangeSelectEdit.SAVE

    override fun serviceSave(value: Account) {
        AccountService.save(value)
    }
}