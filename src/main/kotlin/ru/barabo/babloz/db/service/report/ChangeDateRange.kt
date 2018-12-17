package ru.barabo.babloz.db.service.report

import java.time.LocalDate

interface ChangeDateRange {

    val periodType: PeriodType

    val datePeriods: MutableList<LocalDate>

    fun setDateRange(start: LocalDate, end: LocalDate) {
        val dateRange = DateRange.updateDateRange(start, end, periodType)

        datePeriods.clear()
        datePeriods.addAll(dateRange.getPeriods(periodType))

        updateDateRangeInfo()
    }

    fun updateDateRangeInfo()
}