package com.github.microspectroscopy

import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.text.toUpperCase
import androidx.core.text.isDigitsOnly
import java.util.Locale

fun String.atLeastOneAlpha(): Boolean {
    return this.matches(Regex(".*[a-zA-Z]+.*"))
}

fun String.onlyNumbers(): Boolean {
    return this.matches(Regex("[0-9]+"))
}

val predefined = listOf(
    "pi",
    "e^",
    "1/e",
    "sin",
    "cos",
    "tan",
    "cot",
    "sec",
    "csc",
    "arctan",
    "ln"
)

fun extractVariables(string: String): CharArray {
    return clearPredefined(string.lowercase(Locale.ENGLISH))
        .replace("[^A-Za-z]+".toRegex(), "")
        .toCharArray()
}

fun extractNumbers(string: String): CharArray {
    return clearPredefined(string)
        .replace("[^0-9]+".toRegex(), "")
        .toCharArray()
}

fun clearPredefined(string: String): String {
    var result = string
    for (element in predefined) {
        result = result.replace(element, "")
    }
    return result
}

fun isUsernameValid(username: String): Boolean {
    return username.isNotBlank()
}

fun isNumber(username: String): Boolean {
    return isUsernameValid(username) && username.isDigitsOnly()
}
