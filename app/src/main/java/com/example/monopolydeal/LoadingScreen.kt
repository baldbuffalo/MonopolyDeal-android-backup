package com.example.monopolydeal

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class LoadingScreen : AppCompatActivity() {

    private val loadingDuration: Long = 5000  // 5 seconds
    private val executor = Executors.newSingleThreadExecutor()
    private val githubApiUrl = "https://github.com/baldbuffalo/MonopolyDeal-android-backup/releases"
    private var updateRequired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingScreen()
    }

    private fun showLoadingScreen() {
        setContentView(R.layout.activity_loading)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressText = findViewById<TextView>(R.id.progressText)
        progressBar.visibility = View.VISIBLE
        progressBar.max = 100
        progressText.text = getString(R.string.checking_for_updates)

        executor.execute {
            val latestTag = getLatestGitHubReleaseTag(githubApiUrl)

            runOnUiThread {
                if (latestTag != null) {
                    val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName
                    if (compareVersions(currentVersion, latestTag) < 0) {
                        // A new version is available
                        showUpdatePrompt(githubApiUrl)
                        return@runOnUiThread
                    } else {
                        // The app is up to date, update the progress text
                        progressText.text = getString(R.string.app_up_to_date)
                        // Proceed to the main menu or any other logic
                        navigateToMainMenu()
                        return@runOnUiThread
                    }
                }
                progressText.text = getString(R.string.checking_for_updates_error)
                // Proceed to the main menu or any other logic
                navigateToMainMenu()
                return@runOnUiThread
            }

            val progressHandler = Handler(Looper.getMainLooper())
            progressHandler.postDelayed(object : Runnable {
                var progress = 0

                override fun run() {
                    progressBar.progress = progress
                    progress += 5
                    progressText.text = getString(R.string.checking_for_updates_progress, progress)

                    if (progress <= progressBar.max) {
                        progressHandler.postDelayed(this, loadingDuration / (progressBar.max / 5))
                    } else {
                        // The progress bar has reached 100%
                        progressBar.visibility = View.GONE
                        progressText.visibility = View.GONE

                        // Check if an update is required
                        if (updateRequired) {
                            // Display a custom popup indicating that an update is required
                            showCustomPopup("Update required. Please update the app.")
                        } else {
                            // Stay on the loading screen for an additional 2 seconds
                            progressHandler.postDelayed({
                                // Check for updates has finished, and the progress bar is complete.
                                // Proceed to the main menu.
                                navigateToMainMenu()
                            }, 2000)
                        }
                    }
                }
            }, loadingDuration / (progressBar.max / 5))
        }
    }

    private fun showCustomPopup(message: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // You can add any logic you need after the user clicks OK
                finish()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Alert")
        alert.show()
    }

    private fun showUpdatePrompt(apiUrl: String) {
        updateRequired = true
        val dialogFragment = UpdatePromptDialogFragment.newInstance(apiUrl)
        dialogFragment.show(supportFragmentManager, "update_prompt")
    }

    private fun navigateToMainMenu() {
        val mainMenuIntent = Intent(this, MainMenu::class.java)
        startActivity(mainMenuIntent)
        finish()
    }

    private fun getLatestGitHubReleaseTag(apiUrl: String): String? {
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

            // Parse JSON response to get the tag_name
            val json = response.toString()
            Regex("\"tag_name\":\"(.*?)\"").find(json)?.groupValues?.get(1)
        } catch (e: IOException) {
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

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val apiUrl = arguments?.getString(API_URL_KEY) ?: ""

            return AlertDialog.Builder(requireActivity())
                .setTitle("New Version Available")
                .setMessage("A new version of the app is available. Do you want to download it?")
                .setPositiveButton("Download") { _, _ ->
                    (requireActivity() as? LoadingScreen)?.initiateDownload(apiUrl)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    (requireActivity() as? LoadingScreen)?.navigateToMainMenu()
                }
                .create()
        }
    }

    private fun initiateDownload(apiUrl: String) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(apiUrl)
        val request = DownloadManager.Request(downloadUri)

        // Set up download request properties
        request.setTitle("Monopoly Deal Update")
        request.setDescription("Downloading the latest version of Monopoly Deal")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Set the download destination
        val fileName = "MonopolyDeal.apk"
        request.setDestinationInExternalFilesDir(
            this,
            // Specify the destination folder, you can change
            getExternalFilesDir(null)?.absolutePath,
            fileName
        )

        val downloadId = downloadManager.enqueue(request)
        pollDownloadStatus(downloadManager, downloadId)
    }

    private fun pollDownloadStatus(downloadManager: DownloadManager, downloadId: Long) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed(object : Runnable {
            override fun run() {
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (columnIndex != -1) {
                        val status = cursor.getInt(columnIndex)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                // Download completed successfully, install the APK
                                installApk(cursor)
                            }
                            DownloadManager.STATUS_FAILED -> {
                                // Download failed, proceed to the main menu or any other logic
                                (this@LoadingScreen)?.navigateToMainMenu()
                            }
                            else -> {
                                // Continue polling until the download is complete
                                handler.postDelayed(this, 1000)
                            }
                        }
                    } else {
                        // Handle the case where the column index is -1
                        (this@LoadingScreen)?.navigateToMainMenu()
                    }
                }
                cursor.close()
            }
        }, 1000)
    }

    private fun installApk(cursor: android.database.Cursor) {
        // Get the local URI of the downloaded APK
        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
        if (columnIndex != -1) {
            val localUri = cursor.getString(columnIndex)
            val uri = Uri.parse(localUri)

            // Create an Intent to install the APK
            val installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Start the installation
            startActivity(installIntent)
        }

        // Finish the loading screen activity
        finish()
    }
}
