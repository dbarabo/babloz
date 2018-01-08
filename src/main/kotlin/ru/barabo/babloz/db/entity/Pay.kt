package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@TableName("PAY")
@SelectQuery("select p.id, p.account, a.name ACC_NAME, a.type ACC_TYPE, p.created, " +
        "p.category, c.name CAT_NAME, p.ACCOUNT_TO, ato.name ACCTO_NAME, p.description " +
        "from pay p " +
        "left join account a on p.account = a.id " +
        "left join category c on p.category = c.id " +
        "left join account ato on p.account_to = ato.id  order by p.id")
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
        var created : LocalDate? = LocalDate.now(),

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

        @ColumnName("DESCRIPTION")
        @ColumnType(java.sql.Types.VARCHAR)
        var description :String? = null
        ) {

        val createPay: String get() = created?.let { DateTimeFormatter.ofPattern("dd.MM.yy HH:mm").format(it) } ?: ""

        val fromAccountPay: String get() = account?.name ?: ""

        val namePay: String get() = category?.name ?: accountTo?.name ?: ""

        val sumPay: String get() = amount?.let { DecimalFormat("0.00").format(it) }?:""

        val descriptionPay: String get() = description?.let { it } ?:""
}