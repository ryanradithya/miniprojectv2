package com.example.miniprojectv2

import com.google.firebase.firestore.FirebaseFirestore

data class AppUser(
    val id: String = "",
    val nama: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = ""
)

object UserManager {

    private val db = FirebaseFirestore.getInstance()

    fun generateUserId(
        role: String,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val prefix = if (role == "buyer") "b" else "s"

        db.collection("users")
            .whereEqualTo("role", role)
            .get()
            .addOnSuccessListener { result ->

                val numbers = result.documents
                    .mapNotNull { it.getString("id") }
                    .mapNotNull { it.removePrefix(prefix).toIntOrNull() }

                val nextNumber = (numbers.maxOrNull() ?: 0) + 1
                val newId = prefix + nextNumber

                onComplete(newId)
            }
            .addOnFailureListener(onError)
    }

    // REGISTER USER
    fun registerUser(
        nama: String,
        email: String,
        password: String,
        role: String,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        generateUserId(role,
            onComplete = { newId ->

                val user = AppUser(
                    id = newId,
                    nama = nama,
                    email = email,
                    password = password,
                    role = role
                )

                db.collection("users")
                    .add(user)
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener(onError)
            },
            onError = onError)
    }

    // LOGIN
    fun login(
        usernameOrEmail: String,
        password: String,
        onComplete: (AppUser?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->

                val user = result.documents
                    .mapNotNull { it.toObject(AppUser::class.java) }
                    .find {
                        (it.nama.equals(usernameOrEmail, true) ||
                                it.email.equals(usernameOrEmail, true)) &&
                                it.password == password
                    }

                onComplete(user)
            }
            .addOnFailureListener(onError)
    }

    // UPDATE DATA
    fun updateUser(
        userId: String,
        newName: String,
        newEmail: String,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("id", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onError(Exception("User tidak ditemukan"))
                } else {
                    val docId = result.documents.first().id
                    db.collection("users")
                        .document(docId)
                        .update(
                            mapOf(
                                "nama" to newName,
                                "email" to newEmail
                            )
                        )
                        .addOnSuccessListener { onComplete() }
                        .addOnFailureListener(onError)
                }
            }
            .addOnFailureListener(onError)
    }

    fun emailExists(
        email: String,
        onComplete: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                onComplete(!result.isEmpty)
            }
            .addOnFailureListener(onError)
    }

}
