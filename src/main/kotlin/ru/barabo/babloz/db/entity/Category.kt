package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import ru.barabo.db.converter.EnumConverter

@TableName("CATEGORY")
@SelectQuery("select * from category order by case when parent is null then 100000*id else 100000*parent + id end")
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
    var type :CategoryType = CategoryType.COST
) {
    //override fun toString() = name?:""
}