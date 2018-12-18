package ru.barabo.babloz.db.service.report.restaccount

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.selectValueType
import ru.barabo.babloz.db.service.report.DateRange
import ru.barabo.babloz.db.service.report.PeriodType
import ru.barabo.babloz.db.service.report.categoryturn.processByDates
import java.sql.Date
import java.time.LocalDate
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

object ReportServiceRestAccounts : ReportRestAccounts {

    private val logger = LoggerFactory.getLogger(ReportServiceRestAccounts::class.java)

    override var accountViewType: AccountViewType = AccountViewType.BALANCE
    set(value) {
        field = value

        updateRestAccounts()
    }

    override val entitySet: MutableSet<Account> = LinkedHashSet()

    override val periodType: PeriodType = PeriodType.MONTH

    override val datePeriods: MutableList<LocalDate> = DateRange.minMaxDateList(periodType)

    override val listeners: MutableList<(List<LocalDate>, Map<Account, IntArray>) -> Unit> = ArrayList()

    private val mapRestAccountTurn = HashMap<Account, IntArray>()

    override fun dateListenerList(): List<LocalDate> = datePeriods

    override fun updateEntityInfo(entity: Account) = updateRestAccounts()

    override fun updateDateRangeInfo() = updateRestAccounts()

    override fun infoMap(): Map<Account, IntArray> = mapRestAccountTurn

    private fun updateRestAccounts() {
        mapRestAccountTurn.clear()

        accountViewType.filteredList(entitySet.toList()).forEach {

            mapRestAccountTurn[it] = it.getRestByDates(datePeriods, periodType)
        }

        updateInfoListeners()
    }
}

private const val SELECT_ACCOUNT_REST_DATE = """
   select COALESCE(sum(case when a.ID = p.ACCOUNT then p.AMOUNT else COALESCE(p.amount_to, -1*p.AMOUNT) end), 0)
   from PAY p,
      account a
   where a.ID in (p.ACCOUNT, p.ACCOUNT_TO)
     and COALESCE(p.SYNC, 0) != 2
     and a.ID = ?
     and p.CREATED < ?
"""

private const val SELECT_ACCOUNT_TYPE_REST_DATE = """
   select COALESCE(sum(case when a.ID = p.ACCOUNT then p.AMOUNT else COALESCE(p.amount_to, -1*p.AMOUNT) end), 0)
   from PAY p,
      account a
   where a.ID in (p.ACCOUNT, p.ACCOUNT_TO)
     and COALESCE(p.SYNC, 0) != 2
     and a.TYPE = ?
     and p.CREATED < ?
"""

private const val SELECT_ACCOUNT_ALL_REST_DATE = """
select COALESCE(sum(case when a.ID = p.ACCOUNT then p.AMOUNT else COALESCE(p.amount_to, -1*p.AMOUNT) end), 0)
   from PAY p,
      account a
   where a.ID in (p.ACCOUNT, p.ACCOUNT_TO)
     and COALESCE(p.SYNC, 0) != 2
     and p.CREATED < ?
"""

private fun Account.getRestByDates(dates: List<LocalDate>, periodType: PeriodType): IntArray =

    when {
      id != null ->  processByDates(dates, periodType) { start: Date, _: Date ->
          selectValueType<Number>(SELECT_ACCOUNT_REST_DATE, arrayOf(id, start) )
      }

      type != null -> processByDates(dates, periodType) { start: Date, _: Date ->
          selectValueType<Number>(SELECT_ACCOUNT_TYPE_REST_DATE, arrayOf(type!!.ordinal, start) )
      }

      else -> processByDates(dates, periodType) { start: Date, _: Date ->
            selectValueType<Number>(SELECT_ACCOUNT_ALL_REST_DATE, arrayOf(start) )
        }
    }
