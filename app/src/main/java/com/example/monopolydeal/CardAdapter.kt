package com.example.monopolydeal

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class CardAdapter(private val context: Context, private var cards: List<Card>) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTextView: TextView = itemView.findViewById(R.id.cardTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]

        // Using resource string with placeholders
        val cardText = context.getString(R.string.card_format, card.type, card.value)
        holder.cardTextView.text = cardText
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    fun setCards(newCards: List<Card>) {
        cards = newCards
        notifyDataSetChanged()
    }
}
