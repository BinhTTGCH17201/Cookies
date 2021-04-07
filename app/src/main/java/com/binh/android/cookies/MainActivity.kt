package com.binh.android.cookies

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.binh.android.cookies.data.User
import com.binh.android.cookies.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Cookies)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseDatabase.getInstance().reference.keepSynced(true)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)

        showAddNav()

    }

    private fun showAddNav() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseDatabase.getInstance().reference.child("users/" + user.uid)
            db.get().addOnSuccessListener {
                val thisUser = it.getValue(User::class.java)
                thisUser?.let {
                    if (thisUser.admin == false) {
                        binding.bottomNavigation.menu.removeItem(R.id.addNewPost)
                    }
                }
            }
        } else binding.bottomNavigation.menu.removeItem(R.id.addNewPost)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the options menu from XML
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)

        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity

            setSearchableInfo(
                searchManager.getSearchableInfo(componentName)
            )
            setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
        }

        return true
    }
}