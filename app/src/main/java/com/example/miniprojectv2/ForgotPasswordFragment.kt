package com.example.miniprojectv2

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val emailInput = view.findViewById<EditText>(R.id.email_input)
        val resetButton = view.findViewById<Button>(R.id.btn_reset)
        val backText = view.findViewById<TextView>(R.id.textView_back_login)

        // Kembali ke login
        backText.setOnClickListener {
            (requireActivity() as? LoginLayoutController)?.restoreLoginLayout()
            parentFragmentManager.popBackStack()
        }

        // Kirim email reset password
        resetButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (!validateEmail(email)) return@setOnClickListener

            sendResetEmail(email)
        }
    }

    /**
     * ================= RESET PASSWORD =================
     */
    private fun sendResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                showToast("Link reset password telah dikirim ke email")
                (requireActivity() as? LoginLayoutController)?.restoreLoginLayout()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                showToast("Email tidak terdaftar")
            }
    }

    /**
     * ================= VALIDASI =================
     */
    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email tidak valid")
            return false
        }
        return true
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
