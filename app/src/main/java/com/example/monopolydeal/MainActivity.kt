package com.example.monopolydeal

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import com.example.monopolydeal.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
    private lateinit var database: FirebaseDatabase
    private lateinit var friendsRef: DatabaseReference

    private val friendsList = mutableListOf<String>()
    private var isActivityDestroyed = false

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Check if the user is signed in
        currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, navigate to MainActivity
            navigateToMainActivity()
        } else {
            // User is not signed in, navigate to MainMenu
            navigateToMainMenu()

            // Initialize Monopoly Deal game components
            game = MonopolyDealGame()

            // Example: Set up button click listeners
            binding.drawCardButton.setOnClickListener { drawCard() }
            binding.playCardButton.setOnClickListener { playCard() }
            setUpUsernameButton()
            setUpFriendButton()
        }

        // Set up ActivityResultLauncher
        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the result of the Google Sign-In
                handleGoogleSignInResult(result.data)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startForResult.launch(signInIntent) // Launch using ActivityResultLauncher
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign-In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
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
                    navigateToMainActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    showToast("Authentication failed: ${task.exception?.message}")
                }
            }
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
        showToast("Drew a ${drawnCard.first} with value ${drawnCard.second}")
    }

    private fun playCard() {
        // Implement logic to get the card to play (for example, from the player's hand)
        val playerHand = game.getPlayerHand()
        val cardToPlay: Pair<String, Int>? = playerHand.firstOrNull() // Placeholder, replace with actual logic

        if (cardToPlay != null) {
            // Call the playCard function with the selected card
            val success = game.playCard(cardToPlay)

            // Display a message based on whether the card was played successfully
            if (success) {
                showToast("Played a ${cardToPlay.first} with value ${cardToPlay.second}")
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
        val newFriend = "Friend ${friendsList.size + 1}"
        friendsRef.child(newFriend).setValue(true) // Add friend to Firebase Realtime Database
        showToast("$newFriend added to friends list")
    }

    override fun onDestroy() {
        isActivityDestroyed = true
        // Release Firebase resources if needed
        super.onDestroy()
    }
}
