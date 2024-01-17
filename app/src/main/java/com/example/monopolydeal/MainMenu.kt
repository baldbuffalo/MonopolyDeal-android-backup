package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainMenu : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInButton: ImageButton
    private lateinit var startForResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is already signed in
        val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isSignedIn = preferences.getBoolean("isSignedIn", false)

        if (isSignedIn) {
            // User is already signed in, no need to show the main menu, go to MainActivity directly
            navigateToMainActivity()
            return
        }

        // Continue with the rest of the initialization if the user is not signed in

        setContentView(R.layout.main_menu)

        // Initialize buttons
        googleSignInButton = findViewById(R.id.googleSignInButton)

        // Set click listeners
        googleSignInButton.setOnClickListener { signInWithGoogle() }

        configureGoogleSignIn()

        // Set up the ActivityResultLauncher
        startForResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                // Handle the result in the callback
                val data = result.data
                if (result.resultCode == RESULT_OK && data != null) {
                    handleSignInResult(data)
                } else {
                    // User canceled the sign-in process or signed out
                    Log.w("GoogleSignIn", "Sign-in process canceled or user signed out")
                }
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

    private fun handleSignInResult(data: Intent) {
        val signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = signInAccountTask.getResult(ApiException::class.java)
            // Signed in successfully, navigate to the main activity
            navigateToMainActivity(account!!)
        } catch (e: ApiException) {
            // Handle sign-in failure
            Log.e("SignInFailure", "Google sign-in failed: ${e.statusCode}")
        }
    }

    private fun navigateToMainActivity(account: GoogleSignInAccount? = null) {
        // Set a flag indicating that the user is signed in
        val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        preferences.edit().putBoolean("isSignedIn", true).apply()

        // Print a message indicating the switch
        println("Switching to MainActivity")

        // Start the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        account?.let { intent.putExtra("googleIdToken", it.idToken) }
        startActivity(intent)

        // Finish the current activity (MainMenu)
        finish()
    }
}
