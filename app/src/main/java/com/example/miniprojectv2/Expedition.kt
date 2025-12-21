package com.example.miniprojectv2

import com.google.firebase.firestore.Exclude

data class Expedition(
    var name: String = "",
    var createdAt: Long = 0L
) {
    @get:Exclude
    var id: String = ""
}
