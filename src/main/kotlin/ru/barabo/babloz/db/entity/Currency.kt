package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import kotlin.jvm.Transient

@TableName("CURRENCY")
@SelectQuery("select * from CURRENCY order by ID")
data class Currency (
        @ColumnName("ID")
        @SequenceName("SELECT COALESCE(MIN(*), 0) - 1  from CURRENCY")
        @ColumnType(java.sql.Types.INTEGER)
        var id :Int? = null,

        @ColumnName("NAME")
        @ColumnType(java.sql.Types.VARCHAR)
        var name :String = "",

        @ColumnName("EXT")
        @ColumnType(java.sql.Types.VARCHAR)
        var ext :String? = null,

        @ColumnName("SYNC")
        @ColumnType(java.sql.Types.INTEGER)
        @Transient
        var sync :Int? = null
) {
        override fun toString(): String = name
}
