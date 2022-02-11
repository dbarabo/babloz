package ru.barabo.db

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)

private const val DATE_FORMAT = "yyyy-MM-dd"

private val DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT)

fun localDateFormatToSql(value: Any): String =
    DATE_FORMATTER.format(value as java.time.temporal.TemporalAccessor)

private fun converterToJavaDateTime(value: Any): Any =
    if(value.toString().length == DATE_FORMAT.length) LocalDate.parse(value.toString(), DATE_FORMATTER).atStartOfDay()
    else LocalDateTime.parse(value.toString(), DATE_TIME_FORMATTER)

fun converterToJavaDate(value: Any): LocalDate =
    if(value.toString().length == DATE_FORMAT.length) LocalDate.parse(value.toString(), DATE_FORMATTER)
    else LocalDateTime.parse(value.toString(), DATE_TIME_FORMATTER).toLocalDate()

enum class Type(val clazz: Class<*>, val sqlType: Int,
                private val converterToJava: (Any)->Any,
                private val converterToJavaFromString: (String)->Any?,
                private val increment: (Any)->Any,
                private val converterToDb: (Any)->Any = converterToJava) {

    BYTE(Byte::class.javaObjectType, java.sql.Types.TINYINT, { (it as Number).toByte() }, { it.toByteOrNull() }, { (it as Number).toByte() + 1 }  ),

    SMALLINT(Short::class.javaObjectType, java.sql.Types.SMALLINT, { (it as Number).toShort() }, { it.toShortOrNull() }, { (it as kotlin.Number).toShort() + 1 } ),

    INT(Int::class.javaObjectType, java.sql.Types.INTEGER, { (it as Number).toInt() },  { it.toIntOrNull() }, { (it as kotlin.Number).toInt() + 1 }  ),

    LONG(Long::class.javaObjectType, java.sql.Types.BIGINT, { (it as Number).toLong() }, { it.toLongOrNull() }, { (it as kotlin.Number).toLong() + 1L } ),

    STRING(String::class.javaObjectType, java.sql.Types.VARCHAR, { it.toString() }, { it }, {} ),

    DECIMAL(Double::class.javaObjectType, java.sql.Types.DECIMAL, { (it as Number).toDouble()}, {it.toDoubleOrNull() }, { (it as Number).toDouble() + 1.0} ),

    DATETIME(java.time.LocalDateTime::class.javaObjectType, java.sql.Types.TIMESTAMP,
        ::converterToJavaDateTime,
        ::converterToJavaDateTime,
        {},
        { DATE_TIME_FORMATTER.format(it as java.time.temporal.TemporalAccessor) }
    ),

    DATE(java.time.LocalDate::class.javaObjectType, java.sql.Types.DATE,
        ::converterToJavaDate,
        ::converterToJavaDate,
        {},
        ::localDateFormatToSql
    ),

    /*DATETIME(java.time.LocalDateTime::class.javaObjectType, java.sql.Types.TIMESTAMP,
            { x -> (x as Date).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()},
            { x -> x.toLongOrNull()?.let{ java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() } },
            {},
            {java.sql.Date(Date.from((it as LocalDateTime).atZone(ZoneId.systemDefault()).toInstant()).time) }),

    DATE(java.time.LocalDate::class.javaObjectType, java.sql.Types.DATE,
            { x -> if(x is Date) x.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                else (x as Number).toLong().let{ java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
            },
            { x -> x.toLongOrNull()?.let{ java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() } },
            {},
            {java.sql.Date(Date.from((it as LocalDate).atStartOfDay(ZoneId.systemDefault()).toInstant()).time) }),
    */
    BIG_DECIMAL(BigDecimal::class.javaObjectType, java.sql.Types.NUMERIC,
            {x -> if(x is BigDecimal) x else BigDecimal(x.toString()) },
            { it.toBigDecimalOrNull() },
            { (it as BigDecimal).add(BigDecimal.ONE) },
            { (it as? BigDecimal)?.toString() ?: BigDecimal(it.toString()).toString() });

    companion object {
        private val logger = LoggerFactory.getLogger(Type::class.java)

        private val HASH_CLASS_TYPES = values().associate { Pair(it.clazz, it.sqlType) }

        fun getSqlTypeByClass(clazz: Class<*>) :Int = HASH_CLASS_TYPES[clazz] ?:-1

        private val HASH_TYPES_CLASS = values().associate { Pair(it.sqlType, it.clazz) }

        private val CONVERTER_TO_SQL_BY_SQLTYPE = values().associate { Pair(it.sqlType, it.converterToDb) }

        fun convertToSqlBySqlType(sqlType: Int, javaValue: Any): Any? = CONVERTER_TO_SQL_BY_SQLTYPE[sqlType]?.invoke(javaValue)

        private fun errorNotFoundClassType(typeSql :Int) = "For type $typeSql Class not found"

        @Throws(SessionException::class)
        fun getClassBySqlType(typeSql:Int) :Class<*> {
            logger.info("typeSql=$typeSql")

            return HASH_TYPES_CLASS[typeSql] ?: throw SessionException(errorNotFoundClassType(typeSql))
        }

        fun increment(clazz :Class<*>, value: Any) = HASH_CLASS_INCREMENT[clazz]!!.invoke(value)

        private val HASH_CLASS_INCREMENT = values().associate { Pair(it.clazz, it.increment) }

        private val HASH_CLASS_CONVERTER = values().associate { Pair(it.clazz, it.converterToJava) }

        fun convertValueToJavaTypeByClass(value :Any, clazz :Class<*>) :Any? = HASH_CLASS_CONVERTER[clazz]?.invoke(value)

        private val HASH_CLASS_STRING_CONVERTER =
            values().associate { Pair(it.clazz, it.converterToJavaFromString) }

        fun convertStringValueToJavaByClass(value: String, clazz: Class<*>): Any? =
            HASH_CLASS_STRING_CONVERTER[clazz]?.invoke(value)

        fun isConverterExists(clazz :Class<*>) :Boolean {
            return HASH_CLASS_CONVERTER[clazz] != null
        }

        fun isNumberType(type :Int) :Boolean {
            return when (type) {
                java.sql.Types.INTEGER,
                java.sql.Types.SMALLINT,
                java.sql.Types.BIT,
                java.sql.Types.BIGINT,
                java.sql.Types.FLOAT,
                java.sql.Types.REAL,
                java.sql.Types.DOUBLE,
                java.sql.Types.NUMERIC,
                java.sql.Types.DECIMAL -> true
                else -> false
            }
        }

        fun isDateType(type :Int) :Boolean {
            return when (type) {
                java.sql.Types.DATE,
                java.sql.Types.TIME,
                java.sql.Types.TIMESTAMP -> true
                else -> false
            }
        }

        fun isStringType(type :Int) :Boolean {
            return when (type) {
                java.sql.Types.VARCHAR,
                java.sql.Types.CHAR -> true
                else -> false
            }
        }

        fun localDateToSqlDate(local: LocalDate)  = DATE_FORMATTER.format(local)
                //java.sql.Date(Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant()).time)

        fun localDateToSqlDate(local: LocalDateTime) = DATE_TIME_FORMATTER.format(local)
        //java.sql.Date(Date.from(local.atZone(ZoneId.systemDefault()).toInstant()).time)

    }
}


