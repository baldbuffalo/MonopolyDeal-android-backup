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
        setContentView(R.layout.activity_loading)

        // Find views from the layout
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressText = findViewById<TextView>(R.id.progressText)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()

        // Initially, show the progress bar and update text
        progressBar.visibility = View.VISIBLE
        progressText.text = getString(R.string.checking_for_updates)

        // Simulate some loading process
        Handler(Looper.getMainLooper()).postDelayed({
            checkForUpdates()
            // Once the loading process is done, hide the progress bar
            progressBar.visibility = View.GONE
        }, loadingDuration)
    }

    override fun onResume() {
        super.onResume()
        checkUserAndNavigate()
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
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(githubRepoUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                runOnUiThread {
                    Toast.makeText(this@LoadingScreen, "Failed to check for updates", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    try {
                        val jsonData = it.string()
                        val json = JSONObject(jsonData)

                        // Check if the "tag_name" key exists in the JSON response
                        if (json.has("tag_name")) {
                            val tagName = json.getString("tag_name")
                            val latestVersion = tagName.substring(1) // Assuming tag_name is like "v1.2.3"

                            // Compare with the current version installed on the device
                            if (isNewerVersionAvailable(latestVersion)) {
                                // Prompt the user to download the update
                                runOnUiThread {
                                    showUpdatePrompt()
                                }
                            }
                        } else {
                            // Handle the case where "tag_name" key is not present in the JSON response
                            runOnUiThread {
                                Toast.makeText(this@LoadingScreen, "Invalid JSON response", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@LoadingScreen, "Error parsing JSON response", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun isNewerVersionAvailable(latestVersion: String): Boolean {
        // Get the version code of the installed app
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val installedVersionCode = packageInfo.versionCode

        // Convert the latest version string to an integer
        val latestVersionCode = latestVersion.toIntOrNull()

        // Compare version codes
        return latestVersionCode != null && latestVersionCode > installedVersionCode
    }

    private fun showUpdatePrompt() {
        // Implement the update prompt logic
        // For example, show a dialog or notification to prompt the user to download the update
        Toast.makeText(this, "A new version is available. Please update the app.", Toast.LENGTH_SHORT).show()
    }
}
