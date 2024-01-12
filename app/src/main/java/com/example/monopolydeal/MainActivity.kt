package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.monopolydeal.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var game: MonopolyDealGame
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
    }

    private fun initializeFirebaseComponents() {
        // Optionally, you can start MainActivity directly after a successful sign-in
        startMainActivity()

        // Initialize Monopoly Deal game components
        game = MonopolyDealGame()

        // Example: Set up button click listeners
        binding.drawCardButton.setOnClickListener { drawCard() }
        binding.playCardButton.setOnClickListener { playCard() }
        setUpUsernameButton()
        setUpFriendButton()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.username_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                showOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showOptionsMenu() {
        val popupMenu = PopupMenu(this, binding.usernameButton)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.username_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    signOut()
                    true
                }
                // Add more menu items as needed
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        finish() // Close the current activity
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
                    updateUsernameButton() // Make sure this function is defined
                } else {
                    // If sign in fails, display a message to the user.
                }
            }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun signOut() {
        auth.signOut()
        navigateToMainMenu()
    }

    private fun drawCard() {
        // Add logic to handle drawing a card
        val drawnCard = game.drawCard()
    }

    private fun playCard() {
        // Implement logic to get the card to play (for example, from the player's hand)
        val playerHand = game.getPlayerHand()
        val cardToPlay: Card? = playerHand.firstOrNull() // Placeholder, replace with actual logic

        if (cardToPlay != null) {
            // Call the playCard function with the selected card
            val success = game.playCard(cardToPlay)
        } else {
            // No cards in the player's hand to play
        }
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
                        signOut()
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
        binding.addFriendButton.setOnClickListener {
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
        val buttonText = if (currentUser == null) {
            "Guest $guestCount"
        } else {
            currentUser?.displayName ?: "Guest $guestCount"
        }

        binding.usernameButton.text = buttonText
    }

    override fun onDestroy() {
        isActivityDestroyed = true
        // Release Firebase resources if needed
        auth.removeAuthStateListener(authStateListener)
        super.onDestroy()
    }
}

