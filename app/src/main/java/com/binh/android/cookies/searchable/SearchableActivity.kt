package com.binh.android.cookies.searchable

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.item.StatsTextView
import com.algolia.instantsearch.helper.android.list.autoScrollToStart
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.android.searchbox.connectView
import com.algolia.instantsearch.helper.stats.StatsPresenterImpl
import com.algolia.instantsearch.helper.stats.connectView
import com.binh.android.cookies.R
import com.binh.android.cookies.data.PostSearched
import com.binh.android.cookies.databinding.ActivitySearchableBinding
import com.binh.android.cookies.home.detail.PostDetailsActivity
import com.binh.android.cookies.searchable.adapter.PostSearchAdapter
import com.binh.android.cookies.searchable.viewmodel.PostSearchViewModel
import com.binh.android.cookies.searchable.viewmodel.PostSearchViewModelFactory

class SearchableActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchableBinding
    private lateinit var postSearchViewModel: PostSearchViewModel
    private val connection = ConnectionHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_searchable)

        setSupportActionBar(binding.toolbarSearch)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewModelFactory = PostSearchViewModelFactory(application)

        postSearchViewModel =
            ViewModelProvider(this, viewModelFactory).get(PostSearchViewModel::class.java)

        val adapterPost = PostSearchAdapter(this::onItemClicked)
        postSearchViewModel.posts.observe(this, { hits -> adapterPost.submitList(hits) })
        binding.recipeList.let {
            it.itemAnimator = null
            it.adapter = adapterPost
            it.autoScrollToStart(adapterPost)
        }

        val searchBoxView = SearchBoxViewAppCompat(binding.searchView)
        val statsView = StatsTextView(binding.stats)

        connection += postSearchViewModel.searchBox.connectView(searchBoxView)
        connection += postSearchViewModel.stats.connectView(statsView, StatsPresenterImpl())
    }

    private fun onItemClicked(post: PostSearched, photo: ImageView) {
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

    override fun onDestroy() {
        super.onDestroy()
        connection.clear()
    }
}