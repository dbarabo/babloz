package ru.barabo.babloz.db.service.report

import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class PeriodType(val label: String,
                      val convertDate: (LocalDate)-> LocalDate,
                      val nextDate: (LocalDate)-> LocalDate,
                      val format: (LocalDate)-> String) {

    MONTH("месяц", { it.withDayOfMonth(1) }, { it.plusMonths(1) }, { it.formatMonth() } )
}

fun LocalDate.formatMonth() = DateTimeFormatter.ofPattern("yy.MM").format(this)