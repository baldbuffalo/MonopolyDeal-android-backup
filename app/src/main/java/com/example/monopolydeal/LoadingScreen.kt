package com.example.monopolydeal

import android.app.AlertDialog
import android.util.Log
import android.app.DownloadManager
import androidx.fragment.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import org.json.JSONException

class LoadingScreen : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val githubApiUrl = "https://api.github.com/repos/baldbuffalo/MonopolyDeal-android-backup/releases"
    private val weakReference = WeakReference(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading) // Set your loading screen layout

        // Check for the logoutFlag in the Intent
        val logoutFlag = intent.getBooleanExtra("logoutFlag", false)

        if (logoutFlag) {
            // If logoutFlag is true, go to MainMenu
            navigateToMainMenu()
            return
        }

            // Continue with the normal flow

            // Check authentication state directly in onCreate
            if (FirebaseAuth.getInstance().currentUser != null) {
                // User is signed in, navigate to MainActivity
                finish()
            } else {
                // User is not signed in, show loading screen and check for updates
                showLoadingScreen()
            }
        }

    private fun navigateToMainMenu() {
        Log.d("LoadingScreenActivity", "Navigating to MainMenu")
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoadingScreen() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressText = findViewById<TextView>(R.id.progressText)
        progressBar.visibility = View.VISIBLE
        progressBar.max = 100

        executor.execute {
            // Check for updates
            val latestTag = getLatestGitHubReleaseTag(githubApiUrl)

            runOnUiThread {
                handleGitHubRelease(latestTag, progressBar, progressText)
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    // Function to handle GitHub release
    private fun handleGitHubRelease(latestTag: String?, progressBar: ProgressBar, progressText: TextView) {
        if (latestTag != null) {
            val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

            // Check sign-in status
            val isSignedIn = preferences.getBoolean("isSignedIn", false)

            val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName

            if (compareVersions(currentVersion, latestTag) < 0) {
                // Show update prompt
                showUpdatePrompt(githubApiUrl)

                // Save the latest GitHub release tag in SharedPreferences
                preferences.edit().putString("latestGitHubRelease", latestTag).apply()
            } else {
                // Update progress bar and text to indicate completion
                progressBar.progress = 0
                progressText.text = getString(R.string.loading)

                // Hide progress bar immediately
                progressBar.visibility = View.GONE

                // Check authentication state after progress bar completes
                if (isSignedIn) {
                    navigateToMainActivity()
                } else {
                    navigateToMainMenu()
                }
            }
        } else {
            // Update progress bar and text to indicate error
            progressBar.progress = 0
            progressText.text = getString(R.string.checking_for_updates_error)

            // Show error message and check sign-in status
            showAlertDialog("Error checking for updates. Continue with the normal flow?") { confirmed ->
                if (confirmed) {
                    // Continue with the normal flow

                    // Check sign-in status after showing the alert
                    val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    val isSignedIn = preferences.getBoolean("isSignedIn", false)

                    if (isSignedIn) {
                        // User is signed in, handle accordingly
                    } else {
                        // User is not signed in, handle accordingly
                    }

                    // Continue with the normal flow
                    progressBar.visibility = View.GONE
                } else {
                    // Handle the case where the user did not confirm
                    // You might want to show an additional message or take another action
                }
            }

            // Hide progress bar immediately
            progressBar.visibility = View.GONE
        }
    }


    // Function to show an alert dialog
    private fun showAlertDialog(message: String, onConfirmation: (Boolean) -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirmation(true) }
            .setNegativeButton("No") { _, _ -> onConfirmation(false) }
            .show()
    }

    private fun showCustomPopup(message: String) {
        val activity = weakReference.get()
        if (activity != null) {
            AlertDialog.Builder(activity)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    activity.finish()
                }
                .create()
                .apply {
                    setTitle("Alert")
                    show()
                }
        }
    }

    private fun showUpdatePrompt(apiUrl: String) {
        val activity = weakReference.get()
        if (activity != null) {
            UpdatePromptDialogFragment.newInstance(apiUrl)
                .show(activity.supportFragmentManager, "update_prompt")
        }
    }

    private fun updateRequired(): Boolean {
        // Add your logic to determine if an update is required
        return false
    }

    private fun getLatestGitHubReleaseTag(apiUrl: String): String? {
        val activityRef = WeakReference(this)

        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000

        return try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            val activity = activityRef.get()

            val jsonArray = JSONArray(response.toString())
            if (jsonArray.length() > 0) {
                jsonArray.getJSONObject(0).getString("tag_name")
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun compareVersions(currentVersion: String, latestVersion: String): Int {
        val current = currentVersion.split("\\.")
        val latest = latestVersion.split("\\.")

        val minSize = minOf(current.size, latest.size)
        for (i in 0 until minSize) {
            val currentPart = try {
                current[i].toInt()
            } catch (e: NumberFormatException) {
                Int.MIN_VALUE
            }

            val latestPart = try {
                latest[i].toInt()
            } catch (e: NumberFormatException) {
                Int.MIN_VALUE
            }

            if (currentPart < latestPart) {
                return -1
            } else if (currentPart > latestPart) {
                return 1
            }
        }

        return when {
            current.size < latest.size -> -1
            current.size > latest.size -> 1
            else -> 0
        }
    }

    class UpdatePromptDialogFragment : DialogFragment() {

        companion object {
            private const val API_URL_KEY = "api_url"

            fun newInstance(apiUrl: String): UpdatePromptDialogFragment {
                val fragment = UpdatePromptDialogFragment()
                val args = Bundle()
                args.putString(API_URL_KEY, apiUrl)
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
            val activity = activity
            val apiUrl = arguments?.getString(API_URL_KEY) ?: ""

            return AlertDialog.Builder(activity!!)
                .setTitle("New Version Available")
                .setMessage("A new version of the app is available. Do you want to download it?")
                .setPositiveButton("Download") { _, _ ->
                    (activity as? LoadingScreen)?.initiateDownload(apiUrl)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    (activity as? LoadingScreen)?.finish()
                }
                .create()
        }
    }

    private fun initiateDownload(apiUrl: String) {
        val activity = weakReference.get()
        if (activity != null) {
            val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = Uri.parse(apiUrl)
            val request = DownloadManager.Request(downloadUri)

            request.setTitle("Monopoly Deal Update")
            request.setDescription("Downloading the latest version of Monopoly Deal")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val fileName = "MonopolyDeal.apk"
            request.setDestinationInExternalFilesDir(
                activity,
                activity.getExternalFilesDir(null)?.absolutePath,
                fileName
            )

            val downloadId = downloadManager.enqueue(request)
            pollDownloadStatus(downloadManager, downloadId)
        }
    }

    private fun pollDownloadStatus(downloadManager: DownloadManager, downloadId: Long) {
        val activity = weakReference.get()
        if (activity != null) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val handler = Handler(Looper.getMainLooper())

            lateinit var pollRunnable: Runnable
            var runnableReference = WeakReference<Runnable>(null)

            pollRunnable = Runnable {
                val outerThis = this@LoadingScreen // Capture the outer class reference
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (columnIndex != -1) {
                        val status = cursor.getInt(columnIndex)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> installApk(cursor)
                            DownloadManager.STATUS_FAILED -> (outerThis as? LoadingScreen)?.finish()
                            else -> {
                                val runnable = runnableReference.get()
                                if (runnable != null) {
                                    handler.postDelayed(runnable, 1000)
                                }
                            }
                        }
                    } else {
                        (outerThis as? LoadingScreen)?.finish()
                    }
                }
                cursor.close()
            }

            // Now, it's a var, and we can assign a new value to it
            runnableReference = WeakReference(pollRunnable)

            handler.postDelayed(pollRunnable, 1000)
        }
    }

    private fun installApk(cursor: android.database.Cursor) {
        val activity = weakReference.get()
        if (activity != null) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            if (columnIndex != -1) {
                val localUri = cursor.getString(columnIndex)
                val uri = Uri.parse(localUri)

                val installIntent = Intent(Intent.ACTION_VIEW)
                installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                activity.startActivity(installIntent)
            }

            activity.finish()
        }
    }
}
