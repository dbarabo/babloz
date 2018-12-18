package ru.barabo.babloz.db.service.report.restaccount

import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.service.report.CallBackUpdateListener
import ru.barabo.babloz.db.service.report.ChangeDateRange
import ru.barabo.babloz.db.service.report.ChangeEntity

interface ReportRestAccounts : ChangeEntity<Account>, ChangeDateRange, CallBackUpdateListener<Account> {

    var accountViewType: AccountViewType
}