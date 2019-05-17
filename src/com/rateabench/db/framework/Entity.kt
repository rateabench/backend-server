package com.rateabench.db.framework

import com.rateabench.db.DataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

/**
 * Created by Jonathan Schurmann on 5/10/19.
 */

fun KProperty<*>.name(): String {
    val annotation = findAnnotation<Column>() ?: return name
    if (annotation.name.isEmpty())
        return name
    return annotation.name
}

abstract class Entity<T : Any>(private val cls: KClass<T>) {
    val COLS = getCols()
    abstract val TABLE: String
    abstract val PK: String

    private fun getPrimaryKey(): String {
        if (!cls.annotations.any { it.annotationClass == Table::class })
            throw RuntimeException("Class ${cls.qualifiedName} must be annotated with ${Table::class}")

        for (member in cls.declaredMemberProperties) {
            member.findAnnotation<Id>() ?: continue
            val name = member.name
            return "${this.getTable()}.\"$name\""

        }
        throw RuntimeException("No ${Id::class} annotation found in class ${cls.qualifiedName}")
    }

    fun getSingle(rs: ResultSet): T? = if (rs.next()) parse(rs) else null

    fun getMultiple(rs: ResultSet): List<T> {
        val entities = mutableListOf<T>()
        while (rs.next()) {
            entities.add(parse(rs))
        }
        return entities
    }

    fun executeSimple(query: String, block: PreparedStatement.(conn: Connection) -> List<T>): List<T> {
        val conn = DataSource.getConnection()
        val stmt = conn.prepareStatement(query)
        val entities = block(stmt, conn)
        conn.close()
        return entities
    }

    inline fun prepareStatement(query: String, block: PreparedStatement.(conn: Connection) -> T?): T? {
        val conn = DataSource.getConnection()
        val stmt = conn.prepareStatement(query)
        return block(stmt, conn)
    }

    inline fun <T> execute(connection: Connection, stmt: PreparedStatement, block: ResultSet.() -> T?): T? {
        val rs = stmt.executeQuery()
        val instance = block(rs)
        connection.close()
        return instance
    }

    inline fun <T> transaction(f: Connection.() -> T): T {
        val conn = DataSource.getConnection()
        conn.autoCommit = false
        val res = f(conn)
        conn.commit()
        conn.close()
        return res
    }

    fun parse(rs: ResultSet): T {
        val paramMap = mutableMapOf<KParameter, Any>()
        val fields =
            cls.declaredMemberProperties.filter { it.findAnnotation<Column>() != null || it.findAnnotation<Id>() != null }
        for (field in fields) {
            when (field.returnType.classifier) {
                String::class -> {
                    val colName = field.name()
                    val kParam = cls.primaryConstructor?.findParameterByName(field.name)!!
                    paramMap[kParam] = rs.getString(colName) ?: ""
                }
                Int::class -> {
                    val colName = field.name()
                    val kParam = cls.primaryConstructor?.findParameterByName(field.name)!!
                    paramMap[kParam] = rs.getInt(colName)
                }
                Double::class -> {
                    val colName = field.name()
                    val kParam = cls.primaryConstructor?.findParameterByName(field.name)!!
                    paramMap[kParam] = rs.getDouble(colName)
                }
                Long::class -> {
                    val colName = field.name()
                    val kParam = cls.primaryConstructor?.findParameterByName(field.name)!!
                    paramMap[kParam] = rs.getLong(colName)
                }
                Timestamp::class -> {
                    val colName = field.name()
                    val kParam = cls.primaryConstructor?.findParameterByName(field.name)!!
                    paramMap[kParam] = rs.getTimestamp(colName)
                }
            }
        }
        return cls.primaryConstructor!!.callBy(paramMap)
    }


    private fun getCols(): String {
        if (!cls.annotations.any { it.annotationClass == Table::class })
            throw RuntimeException("Class ${cls.qualifiedName} must be annotated with ${Table::class}")

        val sb = StringBuilder()
        for (member in cls.declaredMemberProperties) {
            val colCls = member.findAnnotation<Column>()
            if (colCls != null) {
                var name: String = if (colCls.name.isEmpty()) member.name else colCls.name
                var refTable = colCls.refTable
                if (refTable.isEmpty()) {
                    refTable = getTable()
                }
                name = "\"$refTable\".\"$name\""
                sb.append("$name,")
            }
        }
        return sb.dropLast(1).toString()
    }


    fun getTable() = cls.findAnnotation<Table>()?.name ?: cls.simpleName!!
}
