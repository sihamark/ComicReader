package de.hamark.comicreader.model

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

class DebugAntilog : Antilog() {
    override fun performLog(
        priority: LogLevel, tag: String?, throwable: Throwable?, message: String?
    ) {
        val p = priority.name.first().uppercase()
        val t = tag?.let { "$it: " } ?: ""
        val stackTrace = throwable?.stackTrace?.joinToString("\n") { "\t$it" } ?:""
        println("$p $t$message $stackTrace")
    }
}