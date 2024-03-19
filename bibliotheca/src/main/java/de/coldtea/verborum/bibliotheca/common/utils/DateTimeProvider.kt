package de.coldtea.verborum.bibliotheca.common.utils

import java.text.SimpleDateFormat
import java.util.Locale


fun getNowInMillis() = System.currentTimeMillis()

fun Long.convertToLocalDateTimeStamp(pattern: String = DEFAULT_DATE_TIME_FORMAT): String{
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(this)
}

private const val DEFAULT_DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm"
