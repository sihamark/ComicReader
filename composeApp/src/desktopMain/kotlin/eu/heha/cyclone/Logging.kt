package eu.heha.cyclone

import io.github.aakira.napier.DebugAntilog
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter

object Logging {
    fun antilog() = DebugAntilog(
        handler = listOf(
            ConsoleHandler().apply {
                level = Level.ALL
                formatter = SimplerFormatter()
            }
        )
    )

    private class SimplerFormatter : SimpleFormatter() {
        override fun format(record: LogRecord): String {
            val date = ZonedDateTime.ofInstant(record.instant, ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val message = formatMessage(record)
            val throwable = record.thrown?.let { thrown ->
                val sw = StringWriter()
                PrintWriter(sw).use {
                    println()
                    thrown.printStackTrace(it)
                }
                sw.toString()
            } ?: ""
            return String.format(
                "%s %s %s\n",
                date,
                message,
                throwable
            )
        }
    }
}