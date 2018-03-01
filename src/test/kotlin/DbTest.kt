
import org.junit.Test
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.BablozQuery
import ru.barabo.babloz.db.entity.Account
import ru.barabo.db.SessionSetting

class DbTest {

    companion object {
        private val logger = LoggerFactory.getLogger(DbTest::class.java)!!
    }


    //@Test
    fun initDb() {
        val data = BablozQuery.select("select * from currency", sessionSetting = SessionSetting(false))

        data.forEach { logger.error(it.joinToString(";")) }
    }

    //@Test
    fun templateAccount() {

        BablozOrm.select(Account::class.java, ::callBackSelectAccount)
    }

    private fun callBackSelectAccount(elem :Account) {

        logger.error("elem=$elem")

        logger.error("id=${elem.id} name=${elem.name} description=${elem.description} type=${elem.type} closed=${elem.closed} rest=${elem.rest}" +
                " currency=${elem.currency} ")
    }
}