package com.sztorm.notecalendar

interface ILogger {
    fun error(throwable: Throwable? = null, message: String? = null, vararg args: Any?)

    fun warning(message: String, vararg args: Any?)

    fun info(message: String, vararg args: Any?)

    fun verbose(message: String, vararg args: Any?)
}