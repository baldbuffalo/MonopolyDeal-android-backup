// LoadingScreen.kt
package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoadingScreen : AppCompatActivity() {

    private val loadingDuration: Long = 2000
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressText = findViewById<TextView>(R.id.progressText)

        auth = FirebaseAuth.getInstance()

        progressBar.visibility = ProgressBar.VISIBLE
        progressText.text = getString(R.string.checking_user_status)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
            progressBar.visibility = ProgressBar.GONE
        }, loadingDuration)
    }

    private fun checkUserAndNavigate() {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser == null) {
            navigateToMainMenu()
        } else {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
