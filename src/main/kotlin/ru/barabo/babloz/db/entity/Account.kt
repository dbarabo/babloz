package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import ru.barabo.db.converter.BooleanConverter
import ru.barabo.db.converter.EnumConverter
import java.math.BigDecimal
import java.time.LocalDate

@TableName("ACCOUNT")
@SelectQuery("""
    select a.ID, a.NAME, a.DESCRIPTION, a.TYPE, a.CLOSED, a.CURRENCY, c.name CUR_NAME, c.EXT CUR_EXT, a.IS_USE_DEBT, a.SYNC,

    (select COALESCE(sum(case when a.ID = p.ACCOUNT then p.AMOUNT else COALESCE(p.amount_to, -1*p.AMOUNT) end), 0)
      from PAY p where a.ID in (p.ACCOUNT, p.ACCOUNT_TO) and COALESCE(p.SYNC, 0) != 2) REST

    from ACCOUNT a, CURRENCY c
    where a.CURRENCY = c.ID
      and (CLOSED IS NULL OR CLOSED > CURRENT_DATE)
      and COALESCE(a.SYNC, 0) != 2
     order by a.TYPE""")
data class Account (
    @ColumnName("ID")
    @SequenceName("SELECT COALESCE(MIN(ID), 0) - 1  from ACCOUNT")
    @ColumnType(java.sql.Types.INTEGER)
    var id :Int? = null,

    @ColumnName("NAME")
    @ColumnType(java.sql.Types.VARCHAR)
    var name :String? = null,

    @ColumnName("DESCRIPTION")
    @ColumnType(java.sql.Types.VARCHAR)
    var description :String? = null,

    @ColumnName("TYPE")
    @ColumnType(java.sql.Types.INTEGER)
    @Converter(EnumConverter::class)
    var type :AccountType? = null,

    @ColumnName("CLOSED")
    @ColumnType(java.sql.Types.DATE)
    var closed :LocalDate? = null,

    @ColumnName("CURRENCY")
    @ColumnType(java.sql.Types.INTEGER)
    @ManyToOne("CUR_")
    var currency :Currency? = null,

    @ColumnName("REST")
    @ColumnType(java.sql.Types.NUMERIC)
    @ReadOnly
    var rest :BigDecimal? = null,

    @ColumnName("IS_USE_DEBT")
    @ColumnType(java.sql.Types.INTEGER)
    @Converter(BooleanConverter::class)
    var isUseDebt: Boolean = false,

    var isSelected : Int? = null,

    @ColumnName("SYNC")
    @ColumnType(java.sql.Types.INTEGER)
    @Transient
    var sync :Int? = null
    ) {

    override fun toString(): String = name?:""

    override fun equals(other: Any?): Boolean {

        if(this === other) return true

        if(other is Account?) {
            return (this.id == other?.id && this.name == other?.name)
        }
        return false
    }

    override fun hashCode(): Int = (id?:0 * 31 + (name?.hashCode()?:0)) * 31
}



