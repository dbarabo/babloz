package ru.barabo.babloz.db.service

import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.entity.Profile
import ru.barabo.db.service.StoreService

object ProfileService: StoreService<Profile, List<Profile>>(BablozOrm, Profile::class.java) {
    override fun elemRoot(): List<Profile> = dataList

    fun dataProfile(): Profile = if(dataList.isEmpty()) Profile() else dataList[0]
}