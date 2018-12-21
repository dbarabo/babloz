package ru.barabo.babloz.gui.formatter

import javafx.scene.control.TextFormatter
import javafx.util.converter.NumberStringConverter
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.util.*
import java.util.function.UnaryOperator

object CurrencyTextFilter : UnaryOperator<TextFormatter.Change?> {

    override fun apply(t: TextFormatter.Change?): TextFormatter.Change? {

        return t?.controlNewText?.parseToMoney()?.let { t }
    }
}

private const val CURRENCY_MASK = "#,###.00"

fun currencyTextFormatter(): TextFormatter<*> {
    return TextFormatter(NumberStringConverter(CURRENCY_MASK), 0, CurrencyTextFilter)
}

fun Number?.toCurrencyFormat() = this?.let{ DecimalFormat(CURRENCY_MASK).format(it) }?:""

fun String?.fromFormatToCurrency() = this?.parseToMoney()?.let { BigDecimal( it.toDouble() ) }


fun String.parseToMoney() :Number? {

    val format = DecimalFormat(CURRENCY_MASK)
    format.negativePrefix = "-"
    format.isParseBigDecimal = true
    format.isDecimalSeparatorAlwaysShown = true
    format.decimalFormatSymbols = DecimalFormatSymbols(Locale("RU"))

    if (this.isEmpty() ) return null

    return try { format.parse(this) } catch (e: ParseException) { null }
}
