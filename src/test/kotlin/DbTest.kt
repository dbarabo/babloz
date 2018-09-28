
import org.junit.Test
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.BablozOrm
import ru.barabo.babloz.db.BablozQuery
import ru.barabo.babloz.db.entity.Account
import ru.barabo.archive.Cmd.JAR_FOLDER
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.db.SessionSetting
import java.math.BigDecimal
import java.nio.file.FileSystems
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

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

    //@Test
    fun testDateTimeFormatter() {

        val value1 = "2016/3/1"

        val value2 = "2016/11/16"

        val formatter = DateTimeFormatter.ofPattern("yyyy/M/d")

        logger.error("${formatter.parse(value1)}")

        logger.error("${formatter.parse(value2)}")
    }

    //@Test
    fun testBigDecimalParse() {

        val symbols = DecimalFormatSymbols()
        //symbols.groupingSeparator = ' '
        symbols.decimalSeparator = ','
        val pattern = "####0.0#"
        val decimalFormat = DecimalFormat(pattern, symbols)
        decimalFormat.isParseBigDecimal = true

        val bigDecimal = decimalFormat.parse("-2062,50") as BigDecimal
        logger.error(bigDecimal.toString())

        val bigDecimal2 = decimalFormat.parse("11150,00") as BigDecimal
        logger.error(bigDecimal2.toString())

        val bigDecimal3 = decimalFormat.parse("-1718987,34") as BigDecimal
        logger.error(bigDecimal3.toString())
    }

    //@Test
    fun testSubString() {
        val text = "[12345]"
        logger.error(text.substring(1, text.length - 1))
    }

    //@Test
    fun testCurrentDir() {


        logger.error("${FileSystems.getDefault().getPath("").toAbsolutePath()}")

        logger.error(JAR_FOLDER.toString())


    }

    //@Test
    fun testReturnFor() {

        val list = 1..10

        list.forEach {
            logger.error("start $it")

            if(it > 0) return@forEach

            logger.error("end $it")
        }
    }

    //@Test
    fun testInstance() {
        val clazz = TestObject::class

        val obj1 = clazz.createInstance()

        val obj2 = clazz.createInstance()

        logger.error("obj1= $obj1")

        logger.error("obj2= $obj2")
    }

    //@Test
    fun copyReflectionTest() {
        val account = Pay(description = "test")

        val copyMethod = account::class.memberFunctions.first { it.name == "copy" }

        val instanceParam = copyMethod.instanceParameter!!

        val result = copyMethod.callBy(mapOf(instanceParam to account))

        logger.error("copy=$result")
    }

}

private object TestObject {

}