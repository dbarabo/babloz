package ru.barabo.babloz.db.entity

import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.db.annotation.*
import ru.barabo.db.converter.EnumConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@TableName("CATEGORY")
@SelectQuery(
"""select c.*,
(select COALESCE(sum(p.AMOUNT), 0) from PAY p, category chi where c.ID in (chi.id, chi.parent) and p.CATEGORY = chi.ID and p.CREATED >= ? and p.CREATED < ?) TURN
from category c where COALESCE(c.SYNC, 0) != 2
order by case when c.parent is null then 100000*c.id else 100000*c.parent + c.id end""")
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
    var isSelected : Int? = null,

    @ColumnName("SYNC")
    @ColumnType(java.sql.Types.INTEGER)
    @Transient
    var sync :Int? = null

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

    val isSelect get() = isSelected?.let { it != 0 } ?: false

    override fun toString(): String = "${if(parent == null)"" else "  ↳"}$name"

    override fun selectParams(): Array<Any?>? = arrayOf(dateStart.toSqlDate(), dateEnd.toSqlDate())

    override fun equals(other: Any?): Boolean {

        if(this === other) return true

        if(other === null || other !is Category) return false

        if(this.id === other.id && this.id != null) return true

        if((this.id === other.id) && (this.name === other.name) ) return true

        return false
    }

    override fun hashCode(): Int = (id?:0 * 31 + (name?.hashCode()?:0)) * 31
}

fun LocalDateTime.toSqlDate() = java.sql.Date(this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
