package com.example.monopolydeal

import android.app.AlertDialog
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
                    checkForUpdates()
                    // Don't navigate here, we want to keep showing the progress bar
                }
            }
        }, loadingDuration / (progressBar.max / 5))
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
                    // After showing the error, continue with the loading screen
                    showCheckingForUpdates()
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
                                    // Keep showing the progress bar after the prompt
                                    showCheckingForUpdates()
                                }
                            } else {
                                // No update available, continue with the loading screen
                                runOnUiThread {
                                    showCheckingForUpdates()
                                }
                            }
                        } else {
                            // Handle the case where "tag_name" key is not present in the JSON response
                            runOnUiThread {
                                Toast.makeText(this@LoadingScreen, "Invalid JSON response", Toast.LENGTH_SHORT).show()
                                showCheckingForUpdates()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@LoadingScreen, "Error parsing JSON response", Toast.LENGTH_SHORT).show()
                            showCheckingForUpdates()
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
        // Show a dialog prompting the user to download the update
        AlertDialog.Builder(this)
            .setTitle("New Version Available")
            .setMessage("A new version of the app is available. Do you want to download it?")
            .setPositiveButton("Download") { _, _ ->
                // Implement the logic to download the update (e.g., open a browser with the download link)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCheckingForUpdates() {
        // Set the text to "Checking for Updates" and continue showing the progress bar
        val progressText = findViewById<TextView>(R.id.progressText)
        progressText.text = getString(R.string.checking_for_updates)
    }
}
