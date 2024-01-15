package com.example.monopolydeal

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.monopolydeal.databinding.ActivityFriendsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class FriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendsBinding
    private lateinit var friendsRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        friendsRef = FirebaseDatabase.getInstance().reference.child("friends").child(currentUser?.uid.orEmpty())

        // Set up the Friends list
        setupFriendsList()
    }

    private fun setupFriendsList() {
        val friendsList = mutableListOf<String>()

        // Read friends from Firebase Realtime Database
        friendsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                friendsList.clear()
                for (friendSnapshot in snapshot.children) {
                    val friendName = friendSnapshot.key
                    if (friendName != null) {
                        friendsList.add(friendName)
                    }
                }

                // Display friends in the ListView
                val adapter = ArrayAdapter(
                    this@FriendsActivity,
                    android.R.layout.simple_list_item_1,
                    friendsList
                )
                binding.friendsListView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }
}
