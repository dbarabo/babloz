package ru.barabo.babloz.db.entity

import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.db.service.PayService.dateEnd
import ru.barabo.babloz.db.service.PayService.dateStart
import ru.barabo.db.annotation.*
import ru.barabo.db.converter.EnumConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@TableName("CATEGORY")
@SelectQuery("select c.*, " +
        "(select COALESCE(sum(p.AMOUNT), 0) from PAY p, category chi where c.ID in (chi.id, chi.parent) and p.CATEGORY = chi.ID and p.CREATED >= ? and p.CREATED < ?) TURN " +
        " from category c " +
        "order by case when c.parent is null then 100000*c.id else 100000*c.parent + c.id end")
data class Category (
    @ColumnName("ID")
    @SequenceName("SELECT COALESCE(MAX(ID), 0) + 1  from CATEGORY")
    @ColumnType(java.sql.Types.INTEGER)
    var id :Int? = null,

    @ColumnName("NAME")
    @ColumnType(java.sql.Types.VARCHAR)
    var name :String? = null,

    @ColumnName("PARENT")
    @ColumnType(java.sql.Types.INTEGER)
    var parent :Int? = null,

    @ColumnName("TYPE")
    @ColumnType(java.sql.Types.INTEGER)
    @Converter(EnumConverter::class)
    var type: CategoryType = CategoryType.COST,

    @ColumnName("TURN")
    @ColumnType(java.sql.Types.NUMERIC)
    @ReadOnly
    var turn : BigDecimal? = null,

    @ColumnName("IS_SELECTED")
    @ColumnType(java.sql.Types.INTEGER)
    @ReadOnly
    var isSelected : Int? = null

) : ParamsSelect {

    companion object {
        val TRANSFER_CATEGORY =  Category(id = null, name = CategoryType.TRANSFER.label, type = CategoryType.TRANSFER)

        private var dateStart: LocalDateTime = LocalDateTime.of(1,1,1, 0, 0)

        private var dateEnd: LocalDateTime = LocalDateTime.of(2100,1,1, 0, 0)

        fun setDatePeriod(start: LocalDate, end: LocalDate) {

            dateStart = start.atStartOfDay()

            dateEnd = end.atTime(LocalTime.MAX)

            CategoryService.initData()
        }

        fun getDatePeriod(): Pair<LocalDate, LocalDate> = Pair(dateStart.toLocalDate(), dateEnd.toLocalDate())
    }

    override fun toString(): String {
        return name?:""
    }

    override fun selectParams(): Array<Any?>? = arrayOf(dateStart.toSqlDate(), dateEnd.toSqlDate())
}

fun LocalDateTime.toSqlDate() = java.sql.Date(this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
