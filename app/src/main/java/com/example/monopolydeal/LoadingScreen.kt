package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class LoadingScreen : AppCompatActivity() {

    private val loadingDuration: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAuthenticationAndNavigate()
        }, loadingDuration)
    }

    private fun checkUserAuthenticationAndNavigate() {
        // Replace this with your actual authentication check logic
        val userLoggedIn = true

        if (userLoggedIn) {
            val intent = Intent(this@LoadingScreen, MainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this@LoadingScreen, MainMenu::class.java)
            startActivity(intent)
        }

        finish()
    }
}
