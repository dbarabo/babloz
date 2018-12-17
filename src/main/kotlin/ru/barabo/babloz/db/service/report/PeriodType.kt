package ru.barabo.babloz.db.service.report

import java.time.LocalDate

enum class PeriodType(val label: String, val convertDate: (LocalDate)-> LocalDate, val nextDate: (LocalDate)-> LocalDate) {

    MONTH("месяц", { it.withDayOfMonth(1) }, { it.plusMonths(1) } )
}