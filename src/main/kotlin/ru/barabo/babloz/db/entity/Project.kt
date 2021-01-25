package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import ru.barabo.db.converter.SqliteLocalDate
import java.math.BigDecimal
import java.time.LocalDate

@TableName("PROJECT")
@SelectQuery("""
        select p.*,
        
       (select sum(case when a.id = pp.account then pp.amount else -1*pp.amount end)
       from pay pp
          , account a
       where pp.PROJECT = p.id
         and a.id in (pp.account, pp.ACCOUNT_TO)
         and a.type = 0) TURN,
                 
       (select min(pp.CREATED)
       from pay pp
          , account a
       where pp.PROJECT = p.id
         and a.id in (pp.account, pp.ACCOUNT_TO)
         and a.type = 0) START_PROJECT,
         
       (select max(pp.CREATED)
       from pay pp
          , account a
       where pp.PROJECT = p.id
         and a.id in (pp.account, pp.ACCOUNT_TO)
         and a.type = 0) END_PROJECT  
           
        from PROJECT p 
        where COALESCE(p.SYNC, 0) != 2 
        order by case when p.parent is null then 100000*p.id else 100000*p.parent + p.id end
""")
data class Project (
        @ColumnName("ID")
        @SequenceName("SELECT COALESCE(MAX(ID), 0) + 1  from PROJECT")
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

        @ColumnName("TURN")
        @ColumnType(java.sql.Types.NUMERIC)
        @ReadOnly
        var turn : BigDecimal? = null,

        @ColumnName("START_PROJECT")
        @ColumnType(java.sql.Types.DATE)
        @Converter(SqliteLocalDate::class)
        @ReadOnly
        var startProject : LocalDate? = null,

        @ColumnName("END_PROJECT")
        @ColumnType(java.sql.Types.DATE)
        @Converter(SqliteLocalDate::class)
        @ReadOnly
        var endProject : LocalDate? = null,

        @ColumnName("SYNC")
        @ColumnType(java.sql.Types.INTEGER)
        @Transient
        var sync :Int? = null
) {
    override fun toString(): String {
        return name?:""
    }
}
