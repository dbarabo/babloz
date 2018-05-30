package ru.barabo.db

import ru.barabo.db.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

open class TemplateQuery (private val query :Query) {

    //private val logger = LoggerFactory.getLogger(TemplateQuery::class.java)

    companion object {

        private fun errorNotFoundAnnotationSelectQuery(className :String?) = "Annotation @SelectQuery not found for class $className"

        private fun errorNotFoundAnnotationTableName(className :String?) = "Annotation @TableName not found for class $className"

        private fun errorNotFoundAnnotationColumnName(className :String?) = "Annotation @ColumnName not found for class $className"

        private fun errorNotFoundAnnotationSequenceName(className :String?) = "Annotation @SequenceName not found for class $className"

        private const val ERROR_NULL_VALUE_TYPE = "Value and Type value is null"

        private fun deleteTemplate(table :String) = "delete from $table where id = ?"

        private fun errorSequenceReturnNull(sequence :String) = "Sequence expression return NULL $sequence"

        private const val ID_COLUMN = "ID"
    }

    fun startLongTransation(): SessionSetting = query.uniqueSession()

    fun commitLongTransaction(sessionSetting: SessionSetting) {
        query.commitFree(sessionSetting)
    }

    fun rollbackLongTransaction(sessionSetting: SessionSetting) {
        query.rollbackFree(sessionSetting)
    }

    @Throws(SessionException::class)
    fun <T> select(select: String, params: Array<Any?>?, row :Class<T>, callBack :(row :T)->Unit) {
        var item :T? = null

        val propertyByColumn = getPropertyByColumn(row)

        query.select(select, params, SessionSetting(false)) lambda@ {
            isNewRow :Boolean, value :Any?, column :String? ->

            if(isNewRow) {
                val newItem = row.newInstance()

                item?.let(callBack)

                item = newItem

                return@lambda
            }

            value ?: return@lambda

            val member = propertyByColumn[column] ?: return@lambda

            val javaValue =  valueToJava(item as Any, value, member, column!!)

            javaValue ?: return@lambda

            member.setter.call(item, javaValue)
        }

        item?.let(callBack)
    }

    @Throws(SessionException::class)
    fun <T> select(row :Class<T>, callBack :(row :T)->Unit) {
        val selectQuery =  getSelect(row)

        val params = if(ParamsSelect::class.java.isAssignableFrom(row)) {
            (row.newInstance() as ParamsSelect).selectParams() } else null

        select(selectQuery, params, row, callBack)
    }

    private fun getPropertyByColumn(row :Class<*>) :Map<String, KMutableProperty<*>> {
        val propertyByColumn = HashMap<String, KMutableProperty<*>>()

        row.kotlin.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
                .filter { it.findAnnotation<ColumnName>()?.name != null}.forEach { member ->

            val columnName = member.findAnnotation<ColumnName>()?.name?.toUpperCase()?:return@forEach

            propertyByColumn.put(columnName, member)

            val prefix = member.findAnnotation<ManyToOne>()?.prefixColumns?.toUpperCase()?:return@forEach

            val subMemberType :Class<*> = member.returnType.javaType as Class<*>

            subMemberType.kotlin.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
                    .filter { it.findAnnotation<ColumnName>()?.name != null}.forEach intern@ {
                val subColumnName =it.findAnnotation<ColumnName>()?.name?.toUpperCase()?:return@intern

                propertyByColumn.put("$prefix$subColumnName", member)
            }
        }

        return propertyByColumn
    }

    @Throws(SessionException::class)
    fun save(item :Any, sessionSetting: SessionSetting = SessionSetting(false)) :EditType {

        val idField = getFieldData(item, ID_COLUMN)

        return if(idField.second is Class<*>) {

            setSequenceValue(item, sessionSetting)

            insert(item, sessionSetting)

            EditType.INSERT

        } else {
            updateById(item, sessionSetting)

            EditType.EDIT
        }
    }

    @Throws(SessionException::class)
    fun deleteById(item :Any) {

        val tableName = getTableName(item)

        val idField = getFieldData(item, ID_COLUMN)

        query.execute(deleteTemplate(tableName), Array(1, {idField.second}))
    }


    @Throws(SessionException::class)
    private fun getSelect(row :Class<*>) :String = row.kotlin.findAnnotation<SelectQuery>()?.name
            ?: throw SessionException(errorNotFoundAnnotationSelectQuery(row.simpleName))

    @Throws(SessionException::class)
    private fun insert (item :Any, sessionSetting : SessionSetting = SessionSetting(false)) {

        val tableName = getTableName(item)

        val fieldsData = getFieldsDataUpdate(item)

        insert(tableName, fieldsData, sessionSetting)
    }

    @Throws(SessionException::class)
    private fun updateById(item :Any, sessionSetting: SessionSetting = SessionSetting(false)) {
        val tableName = getTableName(item)

        val fieldsData = getFieldsDataUpdate(item)

        val idField = fieldsData.firstOrNull { it.first.equals(ID_COLUMN, true) }
                ?: throw SessionException(errorNotFoundAnnotationColumnName(item::class.simpleName))

        val updateFields = fieldsData.filter{!it.first.equals(ID_COLUMN, true)}

        val updateColumns = updateFields.joinToString(" = ?, ",  "", " = ?"){it.first}

        val params = updateFields.map { it.second }.toMutableList()
        params.add(idField.second)

        val updateQuery = updateTemplate(tableName, updateColumns, idField.first)

        query.execute(updateQuery, params.toTypedArray(), sessionSetting)
    }

    private fun setSequenceValue(item :Any, sessionSetting : SessionSetting = SessionSetting(false)) {
        for (member in item::class.declaredMembers) {

            val annotationName = member.findAnnotation<SequenceName>()

            if(annotationName?.name != null){

                val valueSequence = getNextSequenceValue(annotationName.name, sessionSetting)

                (member as KMutableProperty<*>).setter.call(item,
                        Type.convertValueToJavaTypeByClass(valueSequence, member.returnType.javaType as Class<*>))
                return
            }
        }
        throw SessionException(errorNotFoundAnnotationSequenceName(item::class.simpleName))
    }

    @Throws(SessionException::class)
    private fun getNextSequenceValue(sequenceExpression: String, sessionSetting : SessionSetting = SessionSetting(false)) :Any {
        return query.selectValue(sequenceExpression, null, sessionSetting)
                ?: throw SessionException(errorSequenceReturnNull(sequenceExpression))
    }

    private fun updateTemplate(table :String, valueColumns :String, idColumn :String) = "update $table set $valueColumns where $idColumn = ?"


    private fun getInsertQuery(table :String, fields :List<FieldData>) :String {

        val columnNames = fields.joinToString(", ") {it.first}

        val questions = fields.joinToString(", ") { _ -> "?" }

        return "insert into $table ( $columnNames ) values ( $questions )"
    }

    @Throws(SessionException::class)
    private fun getTableName(item :Any) :String = item::class.findAnnotation<TableName>()?.name
            ?: throw SessionException(errorNotFoundAnnotationTableName(item::class.simpleName))


    @Throws(SessionException::class)
    private fun insert(table :String, fields :List<FieldData>, sessionSetting : SessionSetting = SessionSetting(false)) {

        val queryInsert= getInsertQuery(table, fields)

        val params :Array<Any?>? = fields.map { it.second }.toTypedArray()

        query.execute(queryInsert, params, sessionSetting)
    }

    @Throws(SessionException::class)
    private fun getFieldData(item :Any, findColumn :String) :FieldData {
        for (member in item::class.declaredMemberProperties) {
            val annotationName = member.findAnnotation<ColumnName>()

            if(annotationName?.name != null && (findColumn.equals(annotationName.name, true))) {

                val annotationType = member.findAnnotation<ColumnType>()

                val converter = member.findAnnotation<Converter>()?.converterClazz

                return FieldData(annotationName.name,
                        valueToSql(member.call(item), annotationType?.type, converter) )
            }
        }
        throw SessionException(errorNotFoundAnnotationColumnName(item::class.simpleName))
    }

    /**
     * из аннотаций вытаскиваем данные для sql
     */
    @Throws(SessionException::class)
    private fun getFieldsDataUpdate(item :Any) :ArrayList<FieldData> {

        val fieldsData = ArrayList<FieldData>()

        for (member in item::class.declaredMemberProperties) {
            if(member.findAnnotation<ReadOnly>() != null) continue

            val annotationName = member.findAnnotation<ColumnName>()

            if(annotationName?.name != null) {

                val annotationType =member.findAnnotation<ColumnType>()

                val converterClass = member.findAnnotation<Converter>()?.converterClazz

                fieldsData.add(FieldData(annotationName.name,
                        valueToSql(member.call(item), annotationType?.type, converterClass) ))
            }
        }

        if(fieldsData.size == 0) throw SessionException(errorNotFoundAnnotationColumnName(item::class.simpleName))

        return fieldsData
    }

    private fun setMemberValue(clazz :Class<*>, objectMember :Any, value :Any, columnName :String) {

        val member = clazz.kotlin.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>().firstOrNull {
            it.findAnnotation<ColumnName>()?.name?.toUpperCase() == columnName.toUpperCase()
        } ?: return

        val javaValue = valueToJava(objectMember, value, member, columnName) ?: return

        member.setter.call(objectMember, javaValue)
    }

    private fun manyToOneValue(parentItem :Any, member :KMutableProperty<*>, columnName :String, value :Any) :Any? {

        val javaType :Class<*> = member.returnType.javaType as Class<*>

        var objectValue = member.getter.call(parentItem)

        val prefix = member.findAnnotation<ManyToOne>()?.prefixColumns

        val column =
                if(objectValue == null) {
                    objectValue = javaType.newInstance()

                    ID_COLUMN
                } else {
                    columnName.substring(prefix?.length?:0)
                }

        setMemberValue(javaType, objectValue!!, value, column)

        return objectValue
    }

    @Throws(SessionException::class)
    private fun valueToJava(item :Any, value :Any, member :KMutableProperty<*>, columnName :String) :Any? {

        val javaType :Class<*> = member.returnType.javaType as Class<*>

        val converterClass = member.findAnnotation<Converter>()?.converterClazz

        if(converterClass != null) {
            val instance = converterClass.objectInstance ?: converterClass.java.newInstance()

            return (instance as ConverterValue).convertFromBase(value, javaType)
        }

        if(Type.isConverterExists(javaType)) {
            return Type.convertValueToJavaTypeByClass(value, javaType)
        }

        val manyToOneClass =member.findAnnotation<ManyToOne>()

        if(manyToOneClass != null) {

            return manyToOneValue(item, member, columnName, value)
        }


        return value
    }

    /**
     * преобразует значение value к типу type
     * Если value == null => return Class.Type
     */
    @Throws(SessionException::class)
    private fun valueToSql(value :Any?, type :Int?, converterClazz : KClass<*>?) :Any {

        if(value != null && type == null) {
            return value
        }

        if(value == null && type != null) {
            return Type.getClassBySqlType(type)
        }

        if(value == null || type == null) {
            throw SessionException(ERROR_NULL_VALUE_TYPE)
        }

        if(converterClazz != null) {
            val instance = converterClazz.objectInstance ?: converterClazz.java.newInstance()

            return (instance as ConverterValue).convertToBase(value)
        }

        if((value is Number) && Type.isNumberType(type)) {
            return value
        }

        if(Type.isDateType(type)) {
            if(value is Date) {
                return java.sql.Date(value.time)
            }

            if(value is LocalDate) {
                return Type.localDateToSqlDate(value)
            }

            if(value is LocalDateTime) {
                return Type.localDateToSqlDate(value)
            }
        }

        if(value is String && Type.isStringType(type) ) {
            return value
        }

        val (_,  newValue) = getFieldData(value, ID_COLUMN)

        return newValue
    }

}

typealias FieldData = Pair<String, Any>