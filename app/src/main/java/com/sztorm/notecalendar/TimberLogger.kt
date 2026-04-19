package com.sztorm.notecalendar

import timber.log.Timber

object TimberLogger : ILogger {
    override fun error(throwable: Throwable?, message: String?, vararg args: Any?) =
        Timber.e(throwable, message, args)

    override fun warning(message: String, vararg args: Any?) =
        Timber.w(message, args)

    override fun info(message: String, vararg args: Any?) =
        Timber.i(message, args)

    override fun verbose(message: String, vararg args: Any?) =
        Timber.v(message, args)
}