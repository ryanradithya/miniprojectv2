package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        val emailInput = view.findViewById<EditText>(R.id.email_input)
        val newPassInput = view.findViewById<EditText>(R.id.new_password_input)
        val resetButton = view.findViewById<Button>(R.id.btn_reset)

        resetButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val newPassword = newPassInput.text.toString().trim()

            if (email.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updatePassword(email, newPassword)
        }
    }

    private fun updatePassword(email: String, newPassword: String) {

        firestore.collection("users")
            .whereEqualTo("email", email)
        .get()
            .addOnSuccessListener { documents ->

                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "Email tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val userDoc = documents.documents[0].reference

                // Hash password baru
                val hashed = PasswordBcrypt.hashPassword(newPassword)

                userDoc.update("password", hashed)
                    .addOnSuccessListener {

                        Toast.makeText(requireContext(), "Password berhasil diubah!", Toast.LENGTH_SHORT).show()

                        // Kembalikan UI login
                        (requireActivity() as LoginActivity).restoreLoginLayout()

                        // Tutup fragment
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal update password!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Terjadi kesalahan!", Toast.LENGTH_SHORT).show()
            }
    }
}
