package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.monopolydeal.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var game: MonopolyDealGame
    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

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

        // Apply fancy styles
        applyFancyStyles()
    }

    private fun initializeFirebaseComponents() {
        // Optionally, you can start MainActivity directly after a successful sign-in
        startMainActivity()

        // Initialize Monopoly Deal game components
        game = MonopolyDealGame()

        // Example: Set up button click listeners
        binding.drawCardButton.setOnClickListener { drawCard() }
        binding.playCardButton.setOnClickListener { playCard() }
        binding.playButton.setOnClickListener { startMonopolyDealGame() }

        // Call updateUsernameButton here
        updateUsernameButton()
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

    private val logoutRequestCode = 123

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

    private fun drawCard() {
        // Add logic to handle drawing a card
        val drawnCard = game.drawCard()
    }

    private fun playCard() {
        // Implement logic to get the card to play (for example, from the player's hand)
        val playerHand = game.getPlayerHands()
        val cardToPlay: List<Card>? = playerHand.firstOrNull() // Placeholder, replace with actual logic

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

    private fun updateUsernameButton() {
        val buttonText = currentUser?.displayName ?: "Guest"
        binding.usernameButton.text = buttonText
    }

    private fun startMonopolyDealGame() {
        // Start MonopolyDealGame activity
        val intent = Intent(this, MonopolyDealGame::class.java)
        startActivity(intent)
    }

    private fun applyFancyStyles() {
        // Apply custom styles to enhance the UI
        applyButtonStyles(binding.drawCardButton)
        applyButtonStyles(binding.playCardButton)
        applyButtonStyles(binding.playButton)
        applyUsernameButtonStyles(binding.usernameButton)
        // Add more styling as needed
    }

    private fun applyButtonStyles(button: Button) {
        // Apply custom styles to buttons
        val context = button.context
        val resources = context.resources

        button.setTextColor(ContextCompat.getColor(context, R.color.white))
        button.setPadding(
            resources.getDimensionPixelSize(R.dimen.button_padding_horizontal),
            resources.getDimensionPixelSize(R.dimen.button_padding_vertical),
            resources.getDimensionPixelSize(R.dimen.button_padding_horizontal),
            resources.getDimensionPixelSize(R.dimen.button_padding_vertical)
        )
    }

    private fun applyUsernameButtonStyles(button: Button) {
        // Apply custom styles to the username button
        applyButtonStyles(button)
        // Add additional styles as needed
    }

    override fun onDestroy() {
        // Release Firebase resources if needed
        auth.removeAuthStateListener(authStateListener)
        super.onDestroy()
    }
}
