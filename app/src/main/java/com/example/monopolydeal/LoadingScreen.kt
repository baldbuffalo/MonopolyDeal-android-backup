package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoadingScreen : AppCompatActivity() {

    private val loadingDuration: Long = 2000
    private lateinit var auth: FirebaseAuth

    // Updated GitHub repository URL
    private val githubRepoUrl = "https://api.github.com/repos/baldbuffalo/MonopolyDeal-android-backup/releases/latest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Show the loading screen immediately on app launch
        showLoadingScreen()
    }

    private fun showLoadingScreen() {
        // Show the loading screen (you can add your layout inflation logic here)
        setContentView(R.layout.activity_loading)

        // Find views from the layout
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressText = findViewById<TextView>(R.id.progressText)

        // Initially, show the progress bar and update text
        progressBar.visibility = View.VISIBLE
        progressBar.max = 100 // Set the maximum progress value (adjust as needed)
        progressText.text = getString(R.string.checking_for_updates)

        // Simulate some loading process
        val progressHandler = Handler(Looper.getMainLooper())
        progressHandler.postDelayed(object : Runnable {
            var progress = 0
            override fun run() {
                progressBar.progress = progress
                progress += 5 // Adjust the increment value as needed
                if (progress <= progressBar.max) {
                    progressHandler.postDelayed(this, loadingDuration / (progressBar.max / 5))
                } else {
                    // Finish the loading screen when the progress reaches the maximum value
                    progressBar.visibility = View.GONE
                    checkUserAndNavigate()
                }
            }
        }, loadingDuration / (progressBar.max / 5))
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

    private fun checkForUpdates() {
        // Implement your update checking logic here
    }

    private fun isNewerVersionAvailable(latestVersion: String): Boolean {
        // Implement your version comparison logic here
        return false
    }

    private fun showUpdatePrompt() {
        // Implement your update prompt logic here
    }
}
