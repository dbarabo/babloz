package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*

@TableName("PERSON")
@SelectQuery("select * from PERSON")
data class Person (
    @ColumnName("ID")
    @SequenceName("SELECT COALESCE(MAX(ID), 0) + 1  from PERSON")
    @ColumnType(java.sql.Types.INTEGER)
    var id :Int? = null,

    @ColumnName("NAME")
    @ColumnType(java.sql.Types.VARCHAR)
    var name :String? = null,

    @ColumnName("DESCRIPTION")
    @ColumnType(java.sql.Types.VARCHAR)
    var description :String? = null
)