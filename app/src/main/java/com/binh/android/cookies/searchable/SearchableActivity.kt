package com.binh.android.cookies.searchable

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import com.binh.android.cookies.R
import com.binh.android.cookies.data.Post
import com.binh.android.cookies.databinding.ActivitySearchableBinding
import com.binh.android.cookies.home.detail.PostDetailsActivity
import com.binh.android.cookies.searchable.adapter.PostSearchAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

private const val TAG = "SearchableActivity"

class SearchableActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_searchable)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                val searchQuery =
                    FirebaseDatabase.getInstance().reference.child("posts").orderByChild("title")
                        .startAt(query).endAt(query + "\uf8ff")
                Log.d(TAG, "Search keyword: $query")

                val searchOptions = FirebaseRecyclerOptions.Builder<Post>()
                    .setQuery(searchQuery, Post::class.java)
                    .setLifecycleOwner(this)
                    .build()

                val searchAdapter = PostSearchAdapter(searchOptions, this::onItemClicked)

                binding.searchedPost.adapter = searchAdapter
            }
        }
    }

    private fun onItemClicked(post: Post, photo: ImageView) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            photo,
            photo.transitionName
        )

        when (post.postId) {
            "" -> {
                Toast.makeText(this, "Unable to load detail!", Toast.LENGTH_LONG).show()
            }
            else -> {
                val postId = post.postId

                val intent = PostDetailsActivity.getStartIntent(this, postId)
                startActivity(intent, options.toBundle())
            }
        }
    }
}