package com.klyx.editor

import java.io.File

fun scopeNameFromLanguage(language: String, prefix: String = "source") = "$prefix.$language"

fun File.scopeName() = when (extension) {
    "json" -> scopeNameFromLanguage("json")
    "kt" -> scopeNameFromLanguage("kotlin")
    else -> "text.plain"
}
