package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.db.service.StoreService

object PayService : StoreService<Pay, List<Pay>>(BablozOrm){

    override fun elemRoot(): List<Pay> = dataList

    override fun clazz(): Class<Pay> = Pay::class.java
}