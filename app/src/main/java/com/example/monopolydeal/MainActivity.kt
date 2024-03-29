package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.monopolydeal.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var friendsRef: DatabaseReference
    private val friendsList = mutableListOf<String>()
    private var isActivityDestroyed = false
    private var guestCount = 1

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the result of the Google Sign-In
                handleGoogleSignInResult(result.data)
            }
        }

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()

        // Initialize AuthStateListener
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                // User is not signed in, handle it as needed
            } else {
                // User is signed in, proceed with the rest of the initialization
                initializeFirebaseComponents()
            }
        }

        // Register the AuthStateListener
        auth.addAuthStateListener(authStateListener)
        // Explicitly call setUpUsernameButton here
        setUpUsernameButton()
    }

    private fun initializeFirebaseComponents() {
        // Optionally, you can start MainActivity directly after a successful sign-in
        startMainActivity()

        // Example: Set up button click listeners
        binding.playButton.setOnClickListener { startMonopolyDealGame() }

        // Call updateUsernameButton here
        updateUsernameButton()
        setUpFriendButton()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.username_menu, menu)
        return true
    }

    private fun navigateToMainMenu() {
        if (javaClass != MainMenu::class.java) {
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
            finishAffinity() // Close all activities in the task
        }
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        try {
            // Google Sign-In was successful, authenticate with Firebase
            val account =
                GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account?.idToken)
            guestCount++ // Increment guest count
        } catch (e: ApiException) {
            // Google Sign-In failed
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    currentUser = auth.currentUser
                    // Start MainActivity
                    startMainActivity()

                    // Call updateUsernameButton here
                    updateUsernameButton()
                } else {
                    // If sign in fails, display a message to the user.
                }
            }
    }

    private fun startMainActivity() {
        // Use a flag to determine whether to start MainMenu or MainActivity
        val intentClass = if (currentUser == null) {
            MainMenu::class.java
        } else {
            MainActivity::class.java
        }

        val intent = Intent(this, intentClass)
        startActivity(intent)
        finish() // Close the current activity
    }

    private val LOGOUT_REQUEST_CODE = 123

    private fun logout() {
        // Use FirebaseAuth to sign out the current user
        FirebaseAuth.getInstance().signOut()

        // Reset currentUser to null
        currentUser = null

        // Update UI
        updateUsernameButton()

        // Show LoadingScreen activity
        val loadingIntent = Intent(this, LoadingScreen::class.java)
        loadingIntent.putExtra("logoutFlag", true)
        startActivity(loadingIntent)

        // Finish the current activity
        finish()
    }

    private fun setUpUsernameButton() {
        updateUsernameButton()

        binding.usernameButton.setOnClickListener {
            // Create a PopupMenu
            val popupMenu = PopupMenu(this, binding.usernameButton)

            // Inflate the menu resource
            popupMenu.menuInflater.inflate(R.menu.username_menu, popupMenu.menu)

            // Set the item click listener
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.logout -> {
                        logout()
                        true
                    }
                    // Add more menu items as needed
                    else -> false
                }
            }

            // Show the PopupMenu
            popupMenu.show()
        }
    }

    private fun setUpFriendButton() {
        binding.friendsButton.setOnClickListener {
            // Add a friend to the list
            addFriend()
        }
    }

    private fun addFriend() {
        // Check if friendsRef is initialized
        if (::friendsRef.isInitialized) {
            val newFriend = "Friend ${friendsList.size + 1}"
            friendsRef.child(newFriend).setValue(true) // Add friend to Firebase Realtime Database
        }
    }

    private fun updateUsernameButton() {
        val buttonText = currentUser?.displayName ?: "Guest $guestCount"
        binding.usernameButton.text = buttonText
    }

    private fun startMonopolyDealGame() {
        // Add code to start MonopolyDealGame logic directly here
        // For example, you can use methods from the MonopolyDealGame class
        // to initiate and handle the game logic.
    }

    override fun onDestroy() {
        isActivityDestroyed = true
        // Release Firebase resources if needed
        auth.removeAuthStateListener(authStateListener)
        super.onDestroy()
    }
}
