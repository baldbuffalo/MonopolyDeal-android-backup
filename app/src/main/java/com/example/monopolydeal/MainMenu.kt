package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInResult

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

        // Check if the user is already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (account != null) {
            // User is already signed in, proceed to the main activity
            navigateToMainActivity(account)
        }

        startForResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the result in the callback
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                handleGoogleSignInResult(signInResult)
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

    private fun exitApp() {
        // Add any cleanup logic if needed
        finish()
    }

    private fun navigateToMainActivity(account: GoogleSignInAccount) {
        // Start the com.example.monopolydeal.com.example.monopolydeal.com.example.monopolydeal.MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("googleIdToken", account.idToken)
        startActivity(intent)

        // Finish the current activity if needed
        finish()
    }

    private fun handleGoogleSignInResult(result: GoogleSignInResult?) {
        if (result?.isSuccess == true) {
            // Signed in successfully, navigate to the main activity
            val account = result.signInAccount
            navigateToMainActivity(account!!)
        } else {
            // Handle sign-in failure
            val errorMessage = result?.status?.statusMessage ?: "Unknown error"
            Log.e("SignInFailure", errorMessage)
            // Show failure dialog or take appropriate action
        }
    }
}
