// LoadingScreen.kt
package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoadingScreen : AppCompatActivity() {

    private val loadingDuration: Long = 2000
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAuthenticationAndNavigate()
        }, loadingDuration)
    }

    private fun checkUserAuthenticationAndNavigate() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is logged in, navigate to MainActivity
            val intent = Intent(this@LoadingScreen, MainActivity::class.java)
            startActivity(intent)
        } else {
            // User is not logged in, navigate to MainMenu
            val intent = Intent(this@LoadingScreen, MainMenu::class.java)
            startActivity(intent)
        }

        finish()
    }
}
