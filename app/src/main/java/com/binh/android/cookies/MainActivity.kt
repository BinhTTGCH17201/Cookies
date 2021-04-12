package com.binh.android.cookies

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.binh.android.cookies.databinding.ActivityMainBinding
import com.binh.android.cookies.home.postlist.PostListFragmentDirections
import com.binh.android.cookies.home.searchable.SearchableActivity
import com.binh.android.cookies.viewmodel.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private val mainActivityViewModel by viewModels<MainActivityViewModel> {
        MainActivityViewModel.MainActivityViewModelFactory()
    }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this

        setUpNavController()

        getMainActivityState()

        showAddNav()
    }

    private fun setUpNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)
    }

    private fun getMainActivityState() {
        val isPostEdit = intent.getBooleanExtra("EDIT_POST", false)

        if (isPostEdit) {
            val postId = intent.getStringExtra("EDIT_POST_ID")
            navController.navigate(
                PostListFragmentDirections.actionPostListToAddNewPost(
                    postId = postId,
                    editPost = isPostEdit
                )
            )
        }
    }

    private fun showAddNav() {
        mainActivityViewModel.isAdmin.observe(this, {
            binding.bottomNavigation.menu.findItem(R.id.addNewPost).isVisible = it
        })
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
}