package ru.barabo.babloz.db.service.report.restaccount

import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.AsyncProcess
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.selectValueType
import ru.barabo.babloz.db.service.report.DateRange
import ru.barabo.babloz.db.service.report.PeriodType
import ru.barabo.babloz.db.service.report.categoryturn.processByDates
import ru.barabo.db.SESSION_SETTING_KILL
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

        asyncProcess.asyncProcess()
    }

    override val entitySet: MutableSet<Account> = LinkedHashSet()

    override val periodType: PeriodType = PeriodType.MONTH

    override val datePeriods: MutableList<LocalDate> = DateRange.minMaxDateList(periodType)

    override val listeners: MutableList<()->Unit> = ArrayList()

    private var mapRestAccountTurn: Map<Account, IntArray> = HashMap()

    override fun dateListenerList(): List<LocalDate> = synchronized(datePeriods) { ArrayList(datePeriods) }

    override fun updateEntityInfo(entity: Account) = asyncProcess.asyncProcess()

    override fun updateDateRangeInfo() = asyncProcess.asyncProcess()

    override fun infoMap(): Map<Account, IntArray> = synchronized(mapRestAccountTurn) { HashMap(mapRestAccountTurn) }

    private val asyncProcess = AsyncProcess(::updateInfoRestAccounts)

    private fun updateInfoRestAccounts() {
        synchronized(mapRestAccountTurn) { mapRestAccountTurn = getInfoRestAccounts() }

        updateInfoListeners()
    }

    private fun getInfoRestAccounts(): Map<Account, IntArray> {

        val map = HashMap<Account, IntArray>()

        accountViewType.filteredList(entitySet.toList()).forEach {

            map[it] = it.getRestByDates(datePeriods, periodType)
        }

        return map
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
          selectValueType<Number>(SELECT_ACCOUNT_REST_DATE, arrayOf(id, start), SESSION_SETTING_KILL )
      }

      type != null -> processByDates(dates, periodType) { start: Date, _: Date ->
          selectValueType<Number>(SELECT_ACCOUNT_TYPE_REST_DATE, arrayOf(type!!.ordinal, start), SESSION_SETTING_KILL )
      }

      else -> processByDates(dates, periodType) { start: Date, _: Date ->
            selectValueType<Number>(SELECT_ACCOUNT_ALL_REST_DATE, arrayOf(start), SESSION_SETTING_KILL )
        }
    }
