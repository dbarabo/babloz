package ru.barabo.babloz.gui.pay

import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.gui.binding.Saver
import ru.barabo.babloz.gui.custom.ChangeSelectEdit

internal object PaySaver : Saver<Pay, PayBind> {

    override val editBind: PayBind = PayBind(null)
    override val oldBind: PayBind = PayBind(null)

    override var changeSelectEdit: ChangeSelectEdit = ChangeSelectEdit.SAVE

    override fun serviceSave(value: Pay) {
        PayService.save(value)
    }
}