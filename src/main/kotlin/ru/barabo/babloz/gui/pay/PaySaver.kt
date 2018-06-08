package ru.barabo.babloz.gui.pay

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.service.PayService
import ru.barabo.babloz.gui.binding.AbstractSaver

internal object PaySaver : AbstractSaver<Pay, PayBind>(PayBind::class.java) {

    override fun serviceSave(value: Pay) {
        PayService.save(value)
    }

    override fun isDisableEdit(): BooleanBinding =
            Bindings.and(editBind.newPayProperty.isEmpty, super.isDisableEdit())
}