package com.binh.android.cookies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.binh.android.cookies.data.User
import com.binh.android.cookies.databinding.ActivityMainBinding
import com.binh.android.cookies.home.postlist.PostListFragmentDirections
import com.binh.android.cookies.home.searchable.SearchableActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)

        val isPostEdit = intent.getBooleanExtra("EDIT_POST", false)

        if (isPostEdit) {
            Log.d("EDIT_POST", "This post will be edit!")
            val postId = intent.getStringExtra("EDIT_POST_ID")
            navController.navigate(
                PostListFragmentDirections.actionPostListToAddNewPost(
                    postId = postId,
                    editPost = isPostEdit
                )
            )
        } else Log.d("EDIT_POST", "This post will not be edit!")


        showAddNav()
    }

    private fun showAddNav() {
        firebaseAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                val db = FirebaseDatabase.getInstance().reference.child("users/" + user.uid)
                db.get().addOnSuccessListener { snap ->
                    val thisUser = snap.getValue(User::class.java)
                    thisUser?.let {
                        binding.bottomNavigation.menu.findItem(R.id.addNewPost).isVisible =
                            (thisUser.admin)
                    }
                }
            } else binding.bottomNavigation.menu.findItem(R.id.addNewPost).isVisible = false
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuthStateListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the options menu from XML
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.app_bar_search -> {
                val intent = Intent(this, SearchableActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(firebaseAuthStateListener)
    }
}