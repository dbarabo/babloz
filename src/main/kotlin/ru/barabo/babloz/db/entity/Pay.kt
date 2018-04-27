package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import ru.barabo.db.converter.SqliteLocalDate
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@TableName("PAY")
@SelectQuery("select p.id, p.account, a.name ACC_NAME, a.type ACC_TYPE, p.created, " +
        "p.category, c.name CAT_NAME, p.ACCOUNT_TO, ato.name ACCTO_NAME, p.person, per.name PERS_NAME, " +
        "p.project, prj.name PROJ_NAME, p.AMOUNT, p.description " +
        "from pay p " +
        "left join account a on p.account = a.id " +
        "left join category c on p.category = c.id " +
        "left join account ato on p.account_to = ato.id " +
        "left join person per on p.person = per.id " +
        "left join project prj on p.project = prj.id " +
        "order by p.id")
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
        var description :String? = null
        ) {

        val createPay: String get() = created?.let { DateTimeFormatter.ofPattern("dd.MM.yy HH:mm").format(it) } ?: ""

        val fromAccountPay: String get() = account?.name ?: ""

        val namePay: String get() = category?.id?.let { category?.name }
                ?: "${Category.TRANSFER_CATEGORY.name} ${accountToExists()}"

        val sumPay: String get() = amount?.let { DecimalFormat("0.00").format(it) }?:""

        val descriptionPay: String get() = description?.let { it } ?:""

        val projectPay: String get() = project?.name?:""

        val personPay: String get() = person?.name?:""

        private fun fromToAmount() = if(amount?.toDouble()?:0.0 > 0.0) "на " else "с "

        private fun accountToExists() :String = accountTo?.let { "${fromToAmount()}${it.name}" }?:""
}