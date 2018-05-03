package ru.barabo.babloz.db.importer

import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.entity.Project
import ru.barabo.babloz.db.entity.group.GroupAccount
import ru.barabo.babloz.db.entity.group.GroupProject
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.PayService
import java.io.File
import java.math.BigDecimal
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

object CssImporter : Importer {

    //private val logger = LoggerFactory.getLogger(CssImporter::class.java)!!

    override fun import(file: File, charset: Charset) {

        val lines = file.readLines(charset)

        val header = readHeader(lines.first() )

        processRows(lines.drop(1), header)
    }

    private fun readHeader(firstLine: String) : Map<String, Int>{

        val fields = parseCsvQuote(firstLine)

        val fieldMap = HashMap<String, Int>()

        for ( (index, field) in fields.withIndex()) {
            fieldMap += field.toUpperCase() to index
        }

        return fieldMap
    }

    private fun processRows(lines: List<String>, header: Map<String, Int>) {
        lines.forEach {
            val fields = parseCsvQuote(it)

            val pay = createPay(fields, header)

            pay?.let { PayService.save(it) }
        }
    }

    private fun createPay(fields: List<String>, header: Map<String, Int>): Pay? {

        val newPay = Pay(created = null)

        newPay.account = parseAccount( getFieldByHeader(fields, header, PayField.ACCOUNT) ) ?: return null

        newPay.created = parseDate( getFieldByHeader(fields, header, PayField.CREATED) ) ?: return null

        newPay.amount = parseAmount( getFieldByHeader(fields, header, PayField.AMOUNT) ) ?: return null

        newPay.project = parseProject( getFieldByHeader(fields, header, PayField.PROJECT) )

        newPay.description = parseDescription( getFieldByHeader(fields, header, PayField.DESCRIPTION))

        val categoryName = getFieldByHeader(fields, header, PayField.CATEGORY)

        newPay.category = parseCategory( categoryName )

        if(newPay.category == Category.TRANSFER_CATEGORY) {
            newPay.accountTo = parseAccountTo( categoryName )
        }

        return newPay
    }

    private fun parseAccountTo(accountToName: String?): Account? {
        val accountName = if(!accountToName.isNullOrEmpty() ) accountToName!!.withoutBracket() else return null

        val account = parseAccount(accountName)

        return account ?: defaultAccountTo(accountName)
    }

    private fun defaultAccountTo(accountName: String): Account? {

        if(accountName in listOf("Игорь", "Нака") ) return parseAccount("Должен")

        return null
    }

    private fun String.withoutBracket(): String = this.substring(1, this.length - 1)

    private fun parseCategory(categoryName: String?) :Category? {

        val category = if(!categoryName.isNullOrEmpty() ) categoryName!! else return CategoryService.findByName("Хз-куда")

        if(category.first() == '[' && category.last() == ']') return Category.TRANSFER_CATEGORY

        return MAP_CATEGORY[category]
    }

    private val MAP_CATEGORY = mapOf(
            "Банк-ЗП" to CategoryService.findByName("Банк"),
            "Вуокса" to CategoryService.findByName("Вуокса"),
            "ДОМ:инструменты" to CategoryService.findByName("Инструменты"),
            "ДОМ:Коммуналка и тд" to CategoryService.findByName("Коммуналка"),
            "ДОМ:Мебель-ремонт-оборудование" to CategoryService.findByName("Квартира ремонт, материалы"),
            "ДОМ:Огород" to CategoryService.findByName("Огородничество"),
            "ДОМ:Товары для акт. отдыха" to CategoryService.findByName("Инвентарь для акт. Отдыха"),
            "Друзья" to CategoryService.findByName("Подарки друзьям-родств"),
            "Друзья:Отдача долгов" to CategoryService.findByName("Помощь друзьям-родств"),
            "Друзья:Подарки напраздники" to CategoryService.findByName("Подарки друзьям-родств"),
            "Здоровье" to CategoryService.findByName("Услуги"),
            "Ипотека" to CategoryService.findByName("Ипотека"),
            "Кредит:Я+Серега" to CategoryService.findByName("Помощь друзьям-родств"),
            "Магазин+:Еда Зверям (собакен, кошки, рыбы)" to CategoryService.findByName("Домашним питомцам"),
            "Магазин+:Хоз.расходы-бытовая химия" to CategoryService.findByName("Бытовая химия"),
            "Машина:!Расходники-ремонт" to CategoryService.findByName("Расходники"),
            "Машина:!Стоянка" to CategoryService.findByName("Стоянка"),
            "Машина:!Штрафы" to CategoryService.findByName("Штрафы"),
            "Машина:Бензин" to CategoryService.findByName("Бензин"),
            "Машина:Бумажные расходы" to CategoryService.findByName("Бумажные расходы"),
            "Машина:Инструменты" to CategoryService.findByName("Инструмент"),
            "Машина:Ремонт" to CategoryService.findByName("Ремонт"),
            "Нака" to CategoryService.findByName("Нака"),
            "Нака:Ал" to CategoryService.findByName("Нака"),
            "Нака:Ал" to CategoryService.findByName("Нака"),
            "Нака:прочее" to CategoryService.findByName("Подарки друзьям-родств"),
            "ПРОДУКТЫ" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:!Крупы-макарон-МаслРаст-Хлеб" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:!Мясо-Рыба" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:!Молоко-Сыр-Масло-Яйца-Йогурт" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:!Сладости" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:!Фрукты-Овощи" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:Газировка" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:Зверям" to CategoryService.findByName("Домашним питомцам"),
            "ПРОДУКТЫ:Овощи" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:Остальное" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:Рыба" to CategoryService.findByName("Продукты"),
            "ПРОДУКТЫ:Хозрасходы" to CategoryService.findByName("Бытовая химия"),
            "Прочее" to CategoryService.findByName("Нестабильные"),
            "Прочее:от Заработков" to CategoryService.findByName("Заработки"),
            "Прочее:от Подарков" to CategoryService.findByName("Подарки"),
            "Прочее:от Процентов с депозита" to CategoryService.findByName("%% по вкладу-карте"),
            "CashBack" to CategoryService.findByName("Cashback по карте"),
            "Работа" to CategoryService.findByName("Рабочие расходы"),
            "Родственники" to CategoryService.findByName("Подарки друзьям-родств"),
            "Семья" to CategoryService.findByName("Семья"),
            "Семья:!Завтраки-Обеды Детям" to CategoryService.findByName("Завтраки-обеды"),
            "Семья:!Мероприятия-поездки" to CategoryService.findByName("Развлекательные мероприятия"),
            "Семья:!телефоны" to CategoryService.findByName("Телефоны-интернеты"),
            "Семья:!Школа-обучение" to CategoryService.findByName("Школа-обучение"),
            "Семья:Вещи" to CategoryService.findByName("Одежда-обувь"),
            "Семья:Игрушки" to CategoryService.findByName("Гаджеты"),
            "Семья:Образование-Спорт" to CategoryService.findByName("Спорт-секции"),
            "Семья:Продукты" to CategoryService.findByName("Продукты"),
            "Семья:Работа за деньги" to CategoryService.findByName("Работа за деньги"),
            "Семья:связь" to CategoryService.findByName("Телефоны-интернеты"),
            "Серега-Кредит" to CategoryService.findByName("Помощь друзьям-родств"),
            "Я" to CategoryService.findByName("Я"),
            "Я:!алк" to CategoryService.findByName("Алкоголь"),
            "Я:!Больница" to CategoryService.findByName("Лечение"),
            "Я:!Обед" to CategoryService.findByName("Обеды"),
            "Я:!Услуги" to CategoryService.findByName("Услуги"),
            "Я:!ХЗ - куда, потом разберусь" to CategoryService.findByName("Хз-куда"),
            "Я:Развлекухи-мероприятия" to CategoryService.findByName("Развлекухи"),
            "Я:Транспорт" to CategoryService.findByName("Услуги"),
            "Я:Хобби - рыбалка и тд" to CategoryService.findByName("Инвентарь для акт. Отдыха")
    )

    private fun getFieldByHeader(fields: List<String>, header: Map<String, Int>, payField: PayField) :String? {

        val index = header[payField.label]

        return index?.let { if(fields.size > index)fields[index] else null }
    }

    private fun parseDescription(description: String?): String? = description

    private fun parseProject(projectName: String?): Project? =
            projectName?.let { GroupProject.findByDescription(it)?.project }

    private fun parseAmount(amount: String?): BigDecimal? = amount?.let{ decimalFormater().parse(it) as BigDecimal }

    private fun decimalFormater() :DecimalFormat {

        val decimalFormatSymbols = DecimalFormatSymbols().apply { decimalSeparator = ','}

        return DecimalFormat("####0.0#", decimalFormatSymbols).apply { isParseBigDecimal = true }
    }

    private fun parseDate(date: String?) : LocalDateTime? =
            date?.let { LocalDateTime.of(LocalDate.parse(it, DATE_FORMATTER), LocalTime.of(0,0)) }

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")//DateTimeFormatter.ofPattern("yyyy/M/d")

    private fun parseAccount(accountName: String?) :Account?
            = accountName?.let { GroupAccount.findByAccountName(accountName)?.account }

    private const val SEPARATOR = ";"

    private const val CSV_QUOTE = "(\"([^\"]*)\"|[^${SEPARATOR}]*)(${SEPARATOR}|$)"

    private val PATTERN_CSV_QUOTE = Pattern.compile(CSV_QUOTE)

    private fun parseCsvQuote(lineIn: String) :List<String> {

        val line = lineIn.replace("\"".toRegex(), "\n")

        val matcher = PATTERN_CSV_QUOTE.matcher(line)

        val fields = ArrayList<String>()

        while (matcher.find()) {
            var group = matcher.group(1)?:null

            if (group?.isNotEmpty() == true && '"' == group[0]) {
                group = matcher.group(2)
            }

            group = group?.replace("\n".toRegex(), "\"")

            fields.add(group?:"")
        }

        return fields
    }
}