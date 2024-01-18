package com.example.monopolydeal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.monopolydeal.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var game: MonopolyDealGame
    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private var isLoggingOut: Boolean = false
    private var isAuthStateListenerAttached: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up UI components, click listeners, and styles
        initializeUI()

        // Initialize Firebase components
        initializeAuth()
    }

    private fun initializeAuth() {
        auth = FirebaseAuth.getInstance()

        // Check initial authentication state
        currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToMainMenu()
        } else {
            initializeFirebaseComponents()
        }

        // Attach authStateListener only if it's not already attached
        if (!isAuthStateListenerAttached) {
            authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                currentUser = firebaseAuth.currentUser
                if (currentUser == null && !isLoggingOut) {
                    navigateToMainMenu()
                } else {
                    initializeFirebaseComponents()
                }
            }
            auth.addAuthStateListener(authStateListener)

            // Set the flag to indicate that the listener is attached
            isAuthStateListenerAttached = true
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
        binding.playButton.setOnClickListener { startMonopolyDealGame() }

        // Add click listener for the Friends button
        binding.friendsButton.setOnClickListener { showFriendsList() }

        // Call updateUsernameButton here
        updateUsernameButton()
    }

    private fun initializeUI() {
        // Set up UI components, click listeners, and styles
        setUpUsernameButton()
        applyFancyStyles()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.username_menu, menu)
        return true
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        finishAffinity() // Close all activities in the task
    }

    private fun startMainActivity() {
        // Use a flag to determine whether to start MainMenu or MainActivity
        val intentClass = if (currentUser == null) {
            MainMenu::class.java
        } else {
            MainActivity::class.java
        }

        val intent = Intent(this, intentClass)

        // Clear the back stack so that pressing back after logging out doesn't return to the MainActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)

        // Finish the current activity
        finish()
    }

    private fun logout() {
        // Set the flag to true before signing out
        isLoggingOut = true

        // Use FirebaseAuth to sign out the current user
        FirebaseAuth.getInstance().signOut()

        // Finish the current activity
        finish()
    }

    private fun drawCard() {
        // Assuming the drawCard function in MonopolyDealGame takes a playerIndex parameter
        val playerIndex = 0 // Specify the desired player index
        val drawnCard = game.drawCard(playerIndex)
        // Implement drawCard logic as needed
    }

    private fun playCard() {
        // Implement logic to get the card to play (for example, from the player's hand)
        val playerHand = game.getPlayerHands()
        val cardToPlay: List<Card>? = playerHand.firstOrNull() // Placeholder, replace with actual logic

        if (cardToPlay != null) {
            // Specify the playerIndex and cardIndex you want to play (e.g., 0 for the first player and 0 for the first card)
            val playerIndex = 0
            val cardIndex = 0

            // Call the playCard function with both playerIndex and cardIndex
            val success = game.playCard(playerIndex, cardIndex)
            // Implement playCard logic as needed
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

    private fun showFriendsList() {
        // Create an Intent to start FriendsActivity
        val intent = Intent(this, FriendsActivity::class.java)
        startActivity(intent)

        // Finish the current activity (MainActivity)
        finish()
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
        // Remove the AuthStateListener when the activity is destroyed
        auth.removeAuthStateListener(authStateListener)
        super.onDestroy()
    }
}
