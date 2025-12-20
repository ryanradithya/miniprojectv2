package com.example.miniprojectv2

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0) return "-"

    val sdf = SimpleDateFormat(
        "dd MMM yyyy, HH:mm",
        Locale("id", "ID")
    )
    return sdf.format(Date(timestamp))
}
