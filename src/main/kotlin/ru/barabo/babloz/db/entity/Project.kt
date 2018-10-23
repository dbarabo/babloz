package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import kotlin.jvm.Transient

@TableName("PROJECT")
@SelectQuery("select * from PROJECT order by case when parent is null then 100000*id else 100000*parent + id end")
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

        @ColumnName("SYNC")
        @ColumnType(java.sql.Types.INTEGER)
        @Transient
        var sync :Int? = null
) {
    override fun toString(): String {
        return name?:""
    }
}
