package ru.barabo.babloz.gui.pay

import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.gui.binding.AbstractSaver

internal object PaySaver : AbstractSaver<Pay, PayBind>(PayBind::class.java) {

    override fun serviceSave(value: Pay) {
        PayService.save(value)
    }
}