package db.service

import db.service.ListenerAccount.accountRoot
import db.service.ListenerCategory.categoryRoot
import org.junit.Test
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.group.GroupAccount
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.service.AccountService
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.PayService
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import java.math.BigDecimal

class PayServiceTest : StoreListener<List<Pay>>  {

    companion object {
        private val logger = LoggerFactory.getLogger(PayServiceTest::class.java)!!
    }

    //@Test
    fun testReadList() {
        AccountService.addListener(ListenerAccount)

        CategoryService.addListener(ListenerCategory)

        PayService.addListener(this)
    }

    override fun refreshAll(elemRoot: List<Pay>, refreshType: EditType) {

        logger.error("elemRoot.size =${elemRoot.size}")

        elemRoot.forEach { logger.error("$it") }
    }

    //@Test
    fun testInsertPay() {

        val payWithCategory = payWithAccountCategory()

        PayService.save(payWithCategory)

        logger.error("payWithCategory =$payWithCategory")
    }

    private fun payWithAccountCategory(): Pay {

        val account = accountRoot?.child?.first { it.child.isNotEmpty() }?.child!![0].account

        val category=  categoryRoot?.child?.first { it.child.isNotEmpty() }?.category

        return Pay(account = account, category = category, amount = BigDecimal("100.11"), description = "Test with Account & category" )
    }
}

object ListenerAccount :StoreListener<GroupAccount> {

    var accountRoot: GroupAccount? = null

    override fun refreshAll(elemRoot: GroupAccount, refreshType: EditType) {
        accountRoot = elemRoot
    }
}

object ListenerCategory :StoreListener<GroupCategory> {

    var categoryRoot: GroupCategory? = null

    override fun refreshAll(elemRoot: GroupCategory, refreshType: EditType) {
        categoryRoot = elemRoot
    }
}