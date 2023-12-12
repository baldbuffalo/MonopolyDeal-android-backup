// MonopolyDealGame.kt
package com.example.monopolydeal

class  MonopolyDealGame {
    private val playerHand = mutableListOf<Pair<String, Int>>()

    fun drawCard(): Pair<String, Int> {
        // Simplified logic to draw a card
        val drawnCard = Pair("Property", 100)
        playerHand.add(drawnCard)
        return drawnCard
    }

    fun getPlayerHand(): List<Pair<String, Int>> {
        // Return the player's hand
        return playerHand.toList()
    }

    fun playCard(card: Pair<String, Int>): Boolean {
        // Simplified logic to play a card
        if (playerHand.contains(card)) {
            playerHand.remove(card)
            // Add more game-specific logic here
            return true
        }
        return false
    }
}
