package com.example.monopolydeal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
    private val githubRepoUrl =
        "https://api.github.com/repos/baldbuffalo/MonopolyDeal-android-backup/releases/latest"

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
                    Toast.makeText(
                        this@LoadingScreen,
                        "Failed to check for updates",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    try {
                        val jsonData = it.string()
                        val json = JSONObject(jsonData)

                        val tagName = json.getString("tag_name")
                        val latestVersion = tagName.substring(1) // Assuming tag_name is like "v1.2.3"

                        // Compare with the current version installed on the device
                        if (isNewerVersionAvailable(latestVersion)) {
                            // Prompt the user to download the update
                            runOnUiThread {
                                showUpdatePrompt()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun isNewerVersionAvailable(latestVersion: String): Boolean {
        val installedVersion = getVersionCode(this)

        // Compare the latest version with the installed version
        return if (installedVersion != -1L) {
            // Parse the version strings and compare them
            val latestVersionCode = parseVersionCode(latestVersion)
            installedVersion < latestVersionCode
        } else {
            // Unable to determine the installed version, default to true
            true
        }
    }

    private fun getVersionCode(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return -1
    }

    private fun parseVersionCode(version: String): Long {
        // Implement your logic to parse the version string and return the version code
        // For simplicity, let's assume the version string is in the format "X.Y.Z"
        val parts = version.split(".")
        if (parts.size >= 3) {
            return (parts[0].toLong() * 100 + parts[1].toLong() * 10 + parts[2].toLong())
        }
        return -1
    }

    private fun showUpdatePrompt() {
        // Implement the update prompt logic
        // For example, show a dialog or notification to prompt the user to download the update
        Toast.makeText(
            this,
            "A new version is available. Please update the app.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
