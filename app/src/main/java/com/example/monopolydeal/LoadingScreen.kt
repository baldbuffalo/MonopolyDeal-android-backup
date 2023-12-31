package com.example.monopolydeal

import android.app.AlertDialog
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
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class LoadingScreen : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val githubApiUrl = "https://api.github.com/repos/baldbuffalo/MonopolyDeal-android-backup/releases"

    private val weakReference = WeakReference(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is signed in
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is signed in, proceed to MainMenu directly
            navigateToMainMenu()
        } else {
            // User is not signed in, show the loading screen and check for updates
            setContentView(R.layout.activity_loading)
            showLoadingScreen()
        }
    }

    private fun navigateToMainMenu() {
        val activity = weakReference.get()
        if (activity != null) {
            val currentUser = FirebaseAuth.getInstance().currentUser

            val nextActivity = if (currentUser != null) {
                MainActivity::class.java
            } else {
                MainMenu::class.java
            }

            val intent = Intent(activity, nextActivity)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    private fun showLoadingScreen() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressText = findViewById<TextView>(R.id.progressText)
        progressBar.visibility = View.VISIBLE
        progressBar.max = 100
        progressText.text = getString(R.string.checking_for_updates)

        // Execute the background task to check for updates
        executor.execute {
            val latestTag = getLatestGitHubReleaseTag(githubApiUrl)

            runOnUiThread {
                handleGitHubRelease(latestTag, progressText)
            }
        }
    }

    private fun handleGitHubRelease(latestTag: String?, progressText: TextView) {
        if (latestTag != null) {
            val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName
            if (compareVersions(currentVersion, latestTag) < 0) {
                showUpdatePrompt(githubApiUrl)
            } else {
                progressText.text = getString(R.string.app_up_to_date)
                finish() // Directly finish without delay
            }
        } else {
            progressText.text = getString(R.string.checking_for_updates_error)
            finish() // Directly finish without delay
        }
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

            val pollRunnable = object : Runnable {
                override fun run() {
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (columnIndex != -1) {
                            val status = cursor.getInt(columnIndex)
                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> installApk(cursor)
                                DownloadManager.STATUS_FAILED -> (activity as? LoadingScreen)?.finish()
                                else -> handler.postDelayed(this, 1000)
                            }
                        } else {
                            (activity as? LoadingScreen)?.finish()
                        }
                    }
                    cursor.close()
                }
            }

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
