package com.example.miniprojectv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity(), LoginLayoutController {


    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private val RC_GOOGLE_SIGN_IN = 9001

    private val db = FirebaseFirestore.getInstance()
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ===== Status bar =====
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor =
            ContextCompat.getColor(this, R.color.my_custom_status_bar)

        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

        // ===== Google Sign-In config =====
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this, gso)

        val btnGoogleLogin = findViewById<Button>(R.id.btn_google_login)
        btnGoogleLogin.setOnClickListener {
            googleClient.signOut() // prevent cached account bug
            startActivityForResult(googleClient.signInIntent, RC_GOOGLE_SIGN_IN)
        }

        // ===== Login Email / Legacy =====
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val signupText = findViewById<TextView>(R.id.textView5)
        val forgotText = findViewById<TextView>(R.id.textView3)

        loginButton.setOnClickListener {
            val userInput = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (userInput.isEmpty() || password.isEmpty()) {
                toast("Harap isi username/email dan password!")
                return@setOnClickListener
            }

            loginWithFirebase(userInput, password)
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotText.setOnClickListener {
            openForgotPasswordFragment()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0)
                    supportFragmentManager.popBackStack()
                else finish()
            }
        })
    }

    // ================= GOOGLE RESULT =================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                toast("Login Google dibatalkan")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                auth.currentUser?.let { handleGoogleUser(it) }
            }
            .addOnFailureListener {
                toast("Login Google gagal")
            }
    }

    // ================= GOOGLE LOGIN / AUTO REGISTER =================
    private fun handleGoogleUser(user: FirebaseUser) {

        val uid = user.uid
        val email = user.email ?: ""
        val name = user.displayName ?: "User"

        val userRef = db.collection("users").document(uid)

        userRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                // Auto register Google user
                val userData = hashMapOf(
                    "id" to uid,
                    "nama" to name,
                    "email" to email,
                    "role" to "buyer",
                    "authProvider" to "google",
                    "createdAt" to System.currentTimeMillis()
                )
                userRef.set(userData)
            }

            prefs.edit()
                .putString("active_uid", uid)
                .putString("active_username", name)
                .putString("active_email", email)
                .putBoolean("isSeller", false)
                .apply()

            toast("Login Google berhasil")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loginWithFirebase(email: String, password: String) {

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Login harus menggunakan email")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.let { onFirebaseLoginSuccess(it) }
            }
            .addOnFailureListener {
                toast("Email atau password salah")
            }
    }

    private fun onFirebaseLoginSuccess(user: FirebaseUser) {
        val uid = user.uid
        val email = user.email ?: ""

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "buyer"
                val nama = doc.getString("nama") ?: ""

                prefs.edit()
                    .putString("active_uid", uid)
                    .putString("active_username", nama)
                    .putString("active_email", email)
                    .putBoolean("isSeller", role == "seller")
                    .apply()

                startActivity(
                    Intent(
                        this,
                        if (role == "seller") SellerActivity::class.java else MainActivity::class.java
                    )
                )
                finish()
            }
    }


    private fun openForgotPasswordFragment() {
        val bottomHalf = findViewById<View>(R.id.bottom_half)
        val container = findViewById<FrameLayout>(R.id.login_fragment_container)

        bottomHalf.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
        bottomHalf.visibility = View.INVISIBLE

        container.visibility = View.VISIBLE
        container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        supportFragmentManager.beginTransaction()
            .replace(R.id.login_fragment_container, ForgotPasswordFragment())
            .addToBackStack("forgot_password")
            .commit()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun restoreLoginLayout() {
        findViewById<View>(R.id.bottom_half).visibility = View.VISIBLE
        findViewById<FrameLayout>(R.id.login_fragment_container).visibility = View.GONE
    }

}
