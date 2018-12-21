package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import java.math.BigDecimal

@TableName("PERSON")
@SelectQuery("""
    select p.*,

    (select -1* sum(case when a.id = pp.account then pp.amount else -1*pp.amount end)
       from pay pp
          , account a
       where pp.person = p.id
         and coalesce(a.is_use_debt, 0) != 0
         and a.id in (pp.account, pp.ACCOUNT_TO)
         and a.type = 1) DEBT,

    (select sum(case when a.id = pp.account then pp.amount else -1*pp.amount end)
       from pay pp
          , account a
       where pp.person = p.id
         and coalesce(a.is_use_debt, 0) != 0
         and a.id in (pp.account, pp.ACCOUNT_TO)
         and a.type = 2) CREDIT

      from PERSON p
     where COALESCE(p.SYNC, 0) != 2
  order by case when p.parent is null then 100000*p.id else 100000*p.parent + p.id end""")
data class Person (
    @ColumnName("ID")
    @SequenceName("SELECT COALESCE(MAX(ID), 0) + 1  from PERSON")
    @ColumnType(java.sql.Types.INTEGER)
    var id :Int? = null,

    @ColumnName("NAME")
    @ColumnType(java.sql.Types.VARCHAR)
    var name :String? = null,

    @ColumnName("PARENT")
    @ColumnType(java.sql.Types.INTEGER)
    var parent :Int? = null,

    @ColumnName("DESCRIPTION")
    @ColumnType(java.sql.Types.VARCHAR)
    var description :String? = null,

    @ColumnName("SYNC")
    @ColumnType(java.sql.Types.INTEGER)
    @Transient
    var sync :Int? = null,

    @ColumnName("DEBT")
    @ColumnType(java.sql.Types.NUMERIC)
    @ReadOnly
    var debt : BigDecimal? = null,

    @ColumnName("CREDIT")
    @ColumnType(java.sql.Types.NUMERIC)
    @ReadOnly
    var credit : BigDecimal? = null    ) {
    override fun toString(): String = name?:""
}