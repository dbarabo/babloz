package ru.barabo.db

import ru.barabo.db.annotation.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType
import ru.barabo.db.annotation.Transient

internal data class MemberConverter(private val member: KMutableProperty<*>,
                                    private val converter: ConverterValue?, private val manyToOnePrefix: String?) {

    fun setJavaValueToField(newObject: Any, sqlValue: Any, row: Array<Any?>, columnNames: Array<String>): Any? {

        val javaValue = sqlValueToJava(newObject, sqlValue, row, columnNames)

        member.setter.call(newObject, javaValue)

        return javaValue
    }

    fun getSqlValueFromJavaObject(entityItem: Any): Any? {

        val javaValue = member.getter.call(entityItem) ?: return null

        return getSqlValueFromJavaValue(javaValue)
    }

    private fun getSqlValueFromJavaValue(javaValue: Any): Any? {

        return when {
            manyToOnePrefix != null -> getIdSqlValueFromEntity(javaValue)
            converter != null -> converter.convertToBase(javaValue)
            else -> member.javaValueToSql(javaValue)
        }
    }

    private fun getIdSqlValueFromEntity(subEntityItem: Any): Any? {

        val subMember = getIdMember(subEntityItem::class.java) ?: return null

        val javaValue = subMember.getter.call(subEntityItem) ?: return null

        return subMember.javaValueToSql(javaValue)
    }

    private fun sqlValueToJava(newObject: Any, sqlValue: Any, row: Array<Any?>, columnNames: Array<String>): Any? {

        return when {
            manyToOnePrefix != null -> manyToOneJavaObject(newObject, sqlValue, row, columnNames)
            converter != null -> converter.convertFromBase(sqlValue, member.returnType.javaType as Class<*>)
            else -> member.valueToJava(sqlValue)
        }
    }

    private fun manyToOneJavaObject(parentItem: Any, value: Any, row: Array<Any?>, columnNames: Array<String>): Any? {

        val javaType: Class<*> = member.returnType.javaType as Class<*>

        val objectItem = member.getter.call(parentItem) ?: javaType.newInstance()

        setId(javaType, objectItem, value)

        return fillManyToOneColumns(objectItem, columnNames, row)
    }

    private fun fillManyToOneColumns(objectItem: Any, columnNames: Array<String>, row: Array<Any?>): Any {

        val prefixColumn = manyToOnePrefix ?: return objectItem

        val javaType: Class<*> = member.returnType.javaType as Class<*>

        columnNames.forEachIndexed { index, columnName ->

            if(columnName.indexOf(prefixColumn) != 0) return@forEachIndexed

            val subColumn = columnName.substring(prefixColumn.length)

            val valueColumn = row[index] ?: return@forEachIndexed

            setValueSubColumn(javaType, objectItem, valueColumn, subColumn)
        }

        return objectItem
    }

    private fun setId(javaType: Class<*>, objectItem: Any, sqlValue: Any) {

        val memberId = getIdMember(javaType) ?: return

        val javaValue = memberId.valueToJava(sqlValue) ?: return

        memberId.setter.call(objectItem, javaValue)
    }

    private fun setValueSubColumn(javaType: Class<*>, objectItem: Any, sqlValue: Any, columnName: String) {

        val memberCol= getMemberByColumnName(javaType, columnName) ?: return

        val javaValue = memberCol.valueToJava(sqlValue) ?: return

        memberCol.setter.call(objectItem, javaValue)
    }
}



fun getIdPair(entityItem: Any): Pair<String, Any?>? {

    val member = getIdMember(entityItem::class.java) ?: return null

    val columnAnnotation= member.getColumnAnnotation() ?: return null

    return mapMemberToSqlValue(entityItem, columnAnnotation)
}

private fun mapMemberToSqlValue(entityItem: Any, memberColumn: Pair<String, MemberConverter>): Pair<String, Any?> {

    val value = memberColumn.second.getSqlValueFromJavaObject(entityItem)

    return Pair(memberColumn.first, value)
}

fun getInsertListPairs(entityItem: Any): List<Pair<String, Any?>>  =
        getColumnsInsertAnnotation(entityItem::class.java)
                .toPairValueList(entityItem)


fun getUpdateListPairs(entityItem: Any): List<Pair<String, Any?>> =
        getColumnsUpdateAnnotation(entityItem::class.java)
                .toPairValueList(entityItem)

fun getBackupListPairs(entityItem: Any): List<Pair<String, Any?>> =
        getColumnsBackupAnnotation(entityItem::class.java)
                .toPairValueList(entityItem)


private fun Map<String, MemberConverter>.toPairValueList(entityItem: Any): List<Pair<String, Any?>> =
        map { mapMemberToSqlValue(entityItem, Pair(it.key, it.value)) }


/**
 * copy properties with ColumnName annotation (it's not ReadOnly) from this to destination
 */
fun <T: Any> T.setFieldEditValues(destination: T) {

    javaClass.kotlin.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
            .filter { it.findAnnotation<ColumnName>()?.name != null &&
                    it.findAnnotation<ReadOnly>() == null }
            .forEach { member ->

                member.setter.call(destination, member.getter.call(this))
            }
}

fun <T: Any> T.copyByReflection(): T {

    val copyMethod = this::class.memberFunctions.first { it.name == "copy"}

    val instanceParams = copyMethod.instanceParameter!!

    return copyMethod.callBy(mapOf(instanceParams to this)) as T
}

internal fun getColumnsAnnotation(row: Class<*>): Map<String, MemberConverter> = getColumnsAnnotationByFilter(row)

private fun getColumnsInsertAnnotation(row: Class<*>): Map<String, MemberConverter> =
        getColumnsAnnotationByFilter(row) { it.findAnnotation<ColumnName>()?.name != null &&
                it.findAnnotation<ReadOnly>() == null
        }

private fun getColumnsUpdateAnnotation(row: Class<*>): Map<String, MemberConverter> =
        getColumnsAnnotationByFilter(row) { it.findAnnotation<ColumnName>()?.name != null &&
                it.findAnnotation<ReadOnly>() == null &&
                it.findAnnotation<SequenceName>() == null
        }

private fun getColumnsBackupAnnotation(row: Class<*>): Map<String, MemberConverter> =
        getColumnsAnnotationByFilter(row) { it.findAnnotation<ColumnName>()?.name != null &&
                it.findAnnotation<ReadOnly>() == null &&
                it.findAnnotation<Transient>() == null
        }

private fun getColumnsAnnotationByFilter(row: Class<*>,
                                         filtered: (KMutableProperty<*>)->Boolean =
                                                 { it.findAnnotation<ColumnName>()?.name != null}): Map<String, MemberConverter> {

    val columnsAnnotation = HashMap<String, MemberConverter>()

    row.kotlin.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
            .filter { filtered(it) }
            .forEach { member ->

                val columnAnnotation = member.getColumnAnnotation() ?: return@forEach

                columnsAnnotation += columnAnnotation
            }

    return columnsAnnotation
}

fun getColumnsTable(row: Class<*>) = getColumnsByFilter(row) {
    it.findAnnotation<ReadOnly>() == null &&
    it.findAnnotation<Transient>() == null
}

private fun getColumnsByFilter(row: Class<*>, filtered: (KMutableProperty<*>)->Boolean): List<String> =
    row.kotlin.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
            .filter { it.findAnnotation<ColumnName>()?.name != null && filtered(it) }
            .map { it.findAnnotation<ColumnName>()!!.name }


private fun KMutableProperty<*>.getColumnAnnotation(): Pair<String, MemberConverter>? {

    val columnName = findAnnotation<ColumnName>()?.name ?: return null

    val memberConverter = getMemberConvertor() ?: return null

    return Pair(columnName, memberConverter)
}

private fun KMutableProperty<*>.getMemberConvertor(): MemberConverter? {

    val converter = findAnnotation<Converter>()?.converterClazz?.instanceCreateOrGet()

    val manyToOnePrefix = findAnnotation<ManyToOne>()?.prefixColumns

    return MemberConverter(this, converter as? ConverterValue, manyToOnePrefix)
}

private fun KClass<*>.instanceCreateOrGet() = this.objectInstance ?: this.java.newInstance()

private fun getIdMember(javaType: Class<*>): KMutableProperty<*>? = javaType.kotlin.declaredMemberProperties
        .filterIsInstance<KMutableProperty<*>>().firstOrNull { it.findAnnotation<SequenceName>() != null }

private fun getMemberByColumnName(javaType: Class<*>, columnName: String) = javaType.kotlin.declaredMemberProperties
        .filterIsInstance<KMutableProperty<*>>().firstOrNull {
            columnName.equals(it.findAnnotation<ColumnName>()?.name, true)
        }

private fun KMutableProperty<*>.valueToJava(sqlValue: Any): Any? {

    val javaType :Class<*> = returnType.javaType as Class<*>

    return Type.convertValueToJavaTypeByClass(sqlValue, javaType) //SqliteType.sqlValueConvertToJavaValueByJavaType(sqlValue, javaType)
}

private fun KMutableProperty<*>.javaValueToSql(javaValue: Any): Any? =
        findAnnotation<ColumnType>()?.type?.let { getSqlValueBySqlType(it, javaValue) } ?: javaValue

private fun getSqlValueBySqlType(sqlType: Int, javaValue: Any): Any? =
        Type.convertToSqlBySqlType(sqlType, javaValue)