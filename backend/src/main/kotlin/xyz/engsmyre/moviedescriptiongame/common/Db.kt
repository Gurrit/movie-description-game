package xyz.engsmyre.moviedescriptiongame.common

import io.r2dbc.spi.Readable
import org.springframework.r2dbc.core.DatabaseClient

/** Binds [value], or a typed SQL NULL when it is null. */
fun DatabaseClient.GenericExecuteSpec.bindNullable(name: String, value: Any?, type: Class<*>): DatabaseClient.GenericExecuteSpec =
    if (value == null) bindNull(name, type) else bind(name, value)

fun Readable.int(name: String): Int = (get(name, java.lang.Integer::class.java) ?: error("$name is null")).toInt()

fun Readable.intOrNull(name: String): Int? = get(name, java.lang.Integer::class.java)?.toInt()

fun Readable.string(name: String): String = get(name, String::class.java) ?: error("$name is null")

fun Readable.stringOrNull(name: String): String? = get(name, String::class.java)

fun Readable.bool(name: String): Boolean = get(name, java.lang.Boolean::class.java)?.booleanValue() ?: error("$name is null")
