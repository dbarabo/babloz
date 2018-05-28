package ru.barabo.babloz.db

import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class BudgetTypePeriod(val label: String, val dbValue: Int, private val calcDates: (LocalDate)->Pair<LocalDate, LocalDate> ) {

    MONTH("На месяц", 0,
            {it.withDayOfMonth(1) to it.withDayOfMonth(1).plusMonths(1)}),
    YEAR("На год", 1,
            {it.withDayOfYear(1) to it.withDayOfYear(1).plusYears(1)} ),
    QUARTER("На квартал", 2,
            {it.withDayOfMonth(1) to it.withDayOfMonth(1).plusMonths(3)}),
    HALF_YEAR("На полугодие", 3,
            {it.withDayOfMonth(1) to it.withDayOfMonth(1).plusMonths(6)}),
    SPEC_DATE("На заданные даты", 4,
            {it to it});

    override fun toString(): String = label

    fun nameByTypeDate(startDate: LocalDate): String = "$label ${DateTimeFormatter.ofPattern("LLLL").format(startDate)} " +
                "${DateTimeFormatter.ofPattern("yyyy").format(startDate)} года"

    fun getStartEndByDate(startDate: LocalDate): Pair<LocalDate, LocalDate> = calcDates(startDate)
}