package ru.barabo.babloz.db.service.report

import ru.barabo.babloz.db.selectValueType
import java.sql.Date
import java.time.LocalDate


data class DateRange(val startInclusive: LocalDate = LocalDate.now().minusYears(100),
                     val endExclusive: LocalDate = LocalDate.now()) {


    fun getPeriods(periodType: PeriodType): MutableList<LocalDate> {

        val list = ArrayList<LocalDate>()
        var date = startInclusive

        do {
            list += date
            date = periodType.nextDate(date)
        } while (date <= endExclusive)

        list += date

        return list
    }

    companion object {
        private const val SELECT_MIN_PAY_DATE = "select MIN(CREATED) from PAY"

        private val MIN_DATE_PAY =
                (selectValueType<Number>(SELECT_MIN_PAY_DATE)?.let { Date(it.toLong()).toLocalDate() } ?: LocalDate.now())!!

        fun updateDateRange(start: LocalDate = MIN_DATE_PAY, end: LocalDate, periodType: PeriodType): DateRange {

            val startDate = if(start.toEpochDay() < MIN_DATE_PAY.toEpochDay() ) MIN_DATE_PAY else start

            val endDate = if(end.toEpochDay() == LocalDate.MIN.toEpochDay() ) LocalDate.now() else end

            return DateRange(periodType.convertDate(startDate), periodType.convertDate(endDate))
        }

        fun minMaxDateList(periodType: PeriodType) =
                updateDateRange(start = MIN_DATE_PAY, end = LocalDate.now(), periodType = periodType)
    }
}