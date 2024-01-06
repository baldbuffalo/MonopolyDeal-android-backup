package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
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

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the result of the Google Sign-In
                handleGoogleSignInResult(result.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()

        // Check if the user is signed in
        currentUser = auth.currentUser

        if (currentUser == null) {
            // User is not signed in, navigate to MainMenu
            navigateToMainMenu()
        } else {
            // User is signed in, proceed with the rest of the initialization
            initializeFirebaseComponents()
        }
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
        } catch (e: ApiException) {
            // Google Sign-In failed
            showToast("Google Sign-In failed: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    currentUser = auth.currentUser
                    showToast("Google Sign-In successful")
                    // Start MainActivity
                    startMainActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    showToast("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }


    private fun showToast(message: String) {
        if (!isActivityDestroyed) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun signOut() {
        auth.signOut()
        showToast("Signed out")
        navigateToMainMenu()
    }

    private fun drawCard() {
        // Add logic to handle drawing a card
        val drawnCard = game.drawCard()
        showToast("Drew a ${drawnCard.type} with value ${drawnCard.value}")
    }

    private fun playCard() {
        // Implement logic to get the card to play (for example, from the player's hand)
        val playerHand = game.getPlayerHand()
        val cardToPlay: Card? = playerHand.firstOrNull() // Placeholder, replace with actual logic

        if (cardToPlay != null) {
            // Call the playCard function with the selected card
            val success = game.playCard(cardToPlay)

            // Display a message based on whether the card was played successfully
            if (success) {
                showToast("Played a ${cardToPlay.type} with value ${cardToPlay.value}")
            } else {
                showToast("Could not play the selected card")
            }
        } else {
            showToast("No cards in the player's hand to play")
        }
    }

    private fun setUpUsernameButton() {
        updateUsernameButton()

        binding.usernameButton.setOnClickListener {
            // Handle click on the username button
            showToast("Username button clicked")
        }
    }

    private fun updateUsernameButton() {
        val username = currentUser?.displayName ?: "Guest"
        binding.usernameButton.text = username
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
            showToast("$newFriend added to friends list")
        } else {
            showToast("Friends list not initialized. Please sign in first.")
        }
    }

    override fun onDestroy() {
        isActivityDestroyed = true
        // Release Firebase resources if needed
        super.onDestroy()
    }
}
