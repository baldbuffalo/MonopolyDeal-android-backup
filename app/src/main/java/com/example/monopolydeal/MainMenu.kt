// MainMenu.kt
package com.example.monopolydeal

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException

class MainMenu : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInButton: ImageButton
    private lateinit var startForResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        // Initialize buttons
        val exitButton: Button = findViewById(R.id.exitButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)

        // Set click listeners
        exitButton.setOnClickListener { exitApp() }
        googleSignInButton.setOnClickListener { signInWithGoogle() }

        configureGoogleSignIn()

        // Check if Google services are available on the device
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        if (googleApiAvailability.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            // Check if the user is already signed in
            val account = GoogleSignIn.getLastSignedInAccount(this)

            if (account != null) {
                // User is already signed in, proceed to the main activity
                navigateToMainActivity(account)
                return // Make sure to return to avoid executing the rest of the onCreate logic
            }

            startForResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    // Handle the result in the callback
                    val data = result.data
                    if (result.resultCode == RESULT_OK && data != null) {
                        handleSignInResult(data)
                        // Set the signed-in status in SharedPreferences to 1
                        setSignInStatus(1)
                        // Log the status value
                        Log.d("SignInStatus", "Value set to 1")
                    } else {
                        // User canceled the sign-in process or signed out
                        Log.w("GoogleSignIn", "Sign-in process canceled or user signed out")
                        // Set the signed-in status in SharedPreferences to 0
                        setSignInStatus(0)
                        // Log the status value
                        Log.d("SignInStatus", "Value set to 0")
                    }
                }
        } else {
            // Show a message or take appropriate action if Google services are not available
            Log.e("GoogleServices", "Google services are not available on this device.")
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        // Launch the sign-in intent
        val signInIntent = googleSignInClient.signInIntent
        startForResultLauncher.launch(signInIntent)
    }

    private fun exitApp() {
        // Add any cleanup logic if needed
        finish()
    }

    private fun navigateToMainActivity(account: GoogleSignInAccount) {
        // Start the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("googleIdToken", account.idToken)
        startActivity(intent)

        // Finish the current activity if needed
        finish()
    }

    private fun handleSignInResult(data: Intent) {
        val signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = signInAccountTask.getResult(ApiException::class.java)
            // Signed in successfully, navigate to the main activity
            navigateToMainActivity(account!!)
        } catch (e: ApiException) {
            // Handle sign-in failure
            Log.e("SignInFailure", "Google sign-in failed: ${e.statusCode}")
            // Show failure dialog or take appropriate action
        }
    }

    private fun setSignInStatus(status: Int) {
        // Save the sign-in status in SharedPreferences
        val sharedPref: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("sign_in_status", status)
            apply()
        }
    }
}
