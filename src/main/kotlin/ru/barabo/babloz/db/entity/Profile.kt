package ru.barabo.babloz.db.entity

import ru.barabo.babloz.sync.SyncTypes
import ru.barabo.db.annotation.*
import ru.barabo.db.converter.EnumConverter

@TableName("PROFILE")
@SelectQuery("select * from PROFILE LIMIT 1")
class Profile (
        @ColumnName("ID")
        @SequenceName("SELECT COALESCE(MAX(ID), 0) + 1  from PROFILE")
        @ColumnType(java.sql.Types.INTEGER)
        var id :Int? = null,

        @ColumnName("MAIL")
        @ColumnType(java.sql.Types.VARCHAR)
        var mail :String? = null,

        @ColumnName("PSWD_HASH")
        @ColumnType(java.sql.Types.VARCHAR)
        var pswdHash :String? = null,

        @ColumnName("SYNC_TYPE")
        @ColumnType(java.sql.Types.INTEGER)
        @Converter(EnumConverter::class)
        var syncType : SyncTypes? = null,

        @ColumnName("MSG_UID_SYNC")
        @ColumnType(java.sql.Types.BIGINT)
        var msgUidSync :Long? = null
)
