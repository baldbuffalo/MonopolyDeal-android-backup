package com.example.monopolydeal

data class Card(val type: String, val value: Int)

class MonopolyDealGame {

    private val playerHands = mutableListOf<MutableList<Card>>()

    init {
        // Sample initialization with two players
        playerHands.add(mutableListOf(Card("Property", 100), Card("Action", 50)))
        playerHands.add(mutableListOf(Card("Money", 200), Card("Property", 150)))
    }

    fun getPlayerHands(): List<List<Card>> {
        // Return the player hands
        return playerHands.toList()
    }

    fun drawCard(playerIndex: Int) {
        // Sample drawCard logic
        val newCard = Card("New Card", 50)
        playerHands[playerIndex].add(newCard)
    }

    fun playCard(playerIndex: Int, cardIndex: Int): Boolean {
        // Sample playCard logic
        if (playerHands[playerIndex].size > cardIndex) {
            val playedCard = playerHands[playerIndex].removeAt(cardIndex)
            // Implement actions based on the played card
            return true
        }
        return false
    }
}
