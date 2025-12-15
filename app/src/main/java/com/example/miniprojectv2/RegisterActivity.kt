package com.example.miniprojectv2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance()

    private var selectedRole = "buyer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        setupRoleToggle()

        googleClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        findViewById<Button>(R.id.save_button).setOnClickListener {
            registerEmail()
        }

        findViewById<Button>(R.id.btn_google_signup).setOnClickListener {
            startActivityForResult(googleClient.signInIntent, 9002)
        }

        findViewById<TextView>(R.id.textView3).setOnClickListener {
            finish()
        }
    }


    private fun registerEmail() {
        val name = findViewById<EditText>(R.id.input_name).text.toString()
        val email = findViewById<EditText>(R.id.input_email).text.toString()
        val pass = findViewById<EditText>(R.id.input_password).text.toString()
        val dob = findViewById<EditText>(R.id.input_dob).text.toString()
        val region = findViewById<EditText>(R.id.input_region).text.toString()

        if (email.isBlank() || pass.length < 6) return

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                val uid = it.user!!.uid
                val data = hashMapOf(
                    "nama" to name,
                    "email" to email,
                    "role" to selectedRole,
                    "dob" to dob.takeIf { selectedRole == "buyer" },
                    "region" to region.takeIf { selectedRole == "seller" },
                    "authProvider" to "email"
                )
                db.collection("users").document(uid).set(data)
                finish()
            }
    }

    override fun onActivityResult(rc: Int, res: Int, data: Intent?) {
        super.onActivityResult(rc, res, data)
        if (rc == 9002) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)

            val credential =
                GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    val u = it.user!!
                    val profile = hashMapOf(
                        "nama" to (u.displayName ?: ""),
                        "email" to (u.email ?: ""),
                        "role" to selectedRole,
                        "authProvider" to "google"
                    )
                    db.collection("users").document(u.uid).set(profile)
                    finish()
                }
        }
    }

    private fun setupRoleToggle() {

        val btnBuyer = findViewById<MaterialButton>(R.id.btn_buyer)
        val btnSeller = findViewById<MaterialButton>(R.id.btn_seller)

        val nameInput = findViewById<EditText>(R.id.input_name)
        val dobInput = findViewById<EditText>(R.id.input_dob)
        val regionInput = findViewById<EditText>(R.id.input_region)

        fun selectBuyer() {
            selectedRole = "buyer"

            nameInput.hint = "Nama Lengkap"
            dobInput.visibility = View.VISIBLE
            regionInput.visibility = View.GONE

            btnBuyer.setTextColor(getColor(R.color.white))
            btnSeller.setTextColor(getColor(R.color.green_primary))

            btnBuyer.setBackgroundResource(R.drawable.bg_toggle_selected)
            btnSeller.setBackgroundResource(R.drawable.bg_toggle_unselected)

            animateToggle(btnBuyer)
        }

        fun selectSeller() {
            selectedRole = "seller"

            nameInput.hint = "Nama Toko"
            dobInput.visibility = View.GONE
            regionInput.visibility = View.VISIBLE

            btnSeller.setTextColor(getColor(R.color.white))
            btnBuyer.setTextColor(getColor(R.color.green_primary))

            btnSeller.setBackgroundResource(R.drawable.bg_toggle_selected)
            btnBuyer.setBackgroundResource(R.drawable.bg_toggle_unselected)

            animateToggle(btnSeller)
        }

        btnBuyer.setOnClickListener { selectBuyer() }
        btnSeller.setOnClickListener { selectSeller() }

        // DEFAULT
        selectBuyer()
    }


    private fun animateToggle(button: View) {
        button.animate()
            .scaleX(1.03f)
            .scaleY(1.03f)
            .setDuration(120)
            .withEndAction {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .duration = 80
            }
    }

}
