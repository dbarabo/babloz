package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import ru.barabo.db.converter.SqliteLocalDate
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@TableName("PAY")
@SelectQuery(
"""select p.id, p.account, a.name ACC_NAME, a.type ACC_TYPE, p.created,
p.category, c.name CAT_NAME, p.ACCOUNT_TO, ato.name ACCTO_NAME, p.person, per.name PERS_NAME,
p.project, prj.name PROJ_NAME, p.AMOUNT, p.description, p.amount_to, p.SYNC
from pay p
left join account a on p.account = a.id
left join category c on p.category = c.id
left join account ato on p.account_to = ato.id
left join person per on p.person = per.id
left join project prj on p.project = prj.id
where COALESCE(p.SYNC, 0) != 2
order by p.created""")
data class Pay(
        @ColumnName("ID")
        @SequenceName("SELECT COALESCE(MIN(ID), 0) - 1  from PAY")
        @ColumnType(java.sql.Types.INTEGER)
        var id :Int? = null,

        @ColumnName("ACCOUNT")
        @ColumnType(java.sql.Types.INTEGER)
        @ManyToOne("ACC_")
        var account :Account? = null,

        @ColumnName("CREATED")
        @ColumnType(java.sql.Types.DATE)
        @Converter(SqliteLocalDate::class)
        var created : LocalDateTime? = LocalDateTime.now(),

        @ColumnName("CATEGORY")
        @ColumnType(java.sql.Types.INTEGER)
        @ManyToOne("CAT_")
        var category :Category? = null,

        @ColumnName("AMOUNT")
        @ColumnType(java.sql.Types.NUMERIC)
        var amount : BigDecimal? = null,

        @ColumnName("ACCOUNT_TO")
        @ColumnType(java.sql.Types.INTEGER)
        @ManyToOne("ACCTO_")
        var accountTo :Account? = null,

        @ColumnName("PERSON")
        @ColumnType(java.sql.Types.INTEGER)
        @ManyToOne("PERS_")
        var person :Person? = null,

        @ColumnName("PROJECT")
        @ColumnType(java.sql.Types.INTEGER)
        @ManyToOne("PROJ_")
        var project :Project? = null,

        @ColumnName("DESCRIPTION")
        @ColumnType(java.sql.Types.VARCHAR)
        var description :String? = null,

        @ColumnName("AMOUNT_TO")
        @ColumnType(java.sql.Types.NUMERIC)
        var amountTo : BigDecimal? = null,

        @ColumnName("SYNC")
        @ColumnType(java.sql.Types.INTEGER)
        @Transient
        var sync :Int? = null,

        ) {

        var createPay: String = ""
                get() = created?.let { DateTimeFormatter.ofPattern("yy.MM.dd HH:mm").format(it) } ?: ""

        var fromAccountPay: String = ""
                get() = account?.name ?: ""

        var namePay: String = ""
                get() =  category?.id?.let { category?.name } ?: "${Category.TRANSFER_CATEGORY.name} ${accountToExists()}"

        var sumPay: String = ""
                get() = amount?.let { DecimalFormat("0.00").format(it) }?:""

        var descriptionPay: String = ""
                get() = description ?:""

        var projectPay: String = ""
                get() =  project?.name?:""

        var personPay: String = ""
                get() = person?.name?:""

        private fun fromToAmount(): String = if((amount?.toDouble() ?: 0.0) > 0.0) "с " else "на "

        private fun accountToExists() :String = accountTo?.let { "${fromToAmount()}${it.name}" }?:""
}