package db.service

import org.junit.Test
import ru.barabo.babloz.db.entity.GroupAccount
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.db.service.StoreListener

class AccountServiceTest : StoreListener<GroupAccount> {

    override fun refreshAll(elemRoot: GroupAccount) {}

    @Test
    fun testReadList() {
        AccountService.addListener(this)
    }
}