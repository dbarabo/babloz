package ru.barabo.babloz.db.service.report

import java.time.LocalDate

interface ChangeDateRange {

    var dateRange: DateRange

    var periodType: PeriodType

    fun setPeriod(periodType: PeriodType) {

        setDateRange(dateRange.startInclusive, dateRange.endExclusive)
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        dateRange = DateRange.updateDateRange(start, end, periodType)

        updateDateRangeInfo()
    }

    fun updateDateRangeInfo()

    fun dateRangeByList(): List<LocalDate> = synchronized(dateRange) {
        synchronized(periodType) { dateRange.getPeriods(periodType) }
    }
}