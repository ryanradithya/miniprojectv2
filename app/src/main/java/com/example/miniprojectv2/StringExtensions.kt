package com.example.miniprojectv2.utils

fun String.toTitleCase(): String {
    return lowercase()
        .split(" ")
        .joinToString(" ") {
            it.replaceFirstChar { c -> c.uppercase() }
        }
}
