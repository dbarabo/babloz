package db.service

import org.junit.Test
import ru.barabo.babloz.db.entity.group.GroupAccount
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener

class AccountServiceTest : StoreListener<GroupAccount> {

    override fun refreshAll(elemRoot: GroupAccount, refreshType: EditType) {}

    //@Test
    fun testReadList() {
        AccountService.addListener(this)
    }
}