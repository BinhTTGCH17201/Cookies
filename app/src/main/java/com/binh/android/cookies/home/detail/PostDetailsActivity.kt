package com.binh.android.cookies.home.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil
import com.binh.android.cookies.MainActivity
import com.binh.android.cookies.R
import com.binh.android.cookies.data.Comment
import com.binh.android.cookies.databinding.ActivityPostDetailsBinding
import com.binh.android.cookies.home.detail.adapter.CommentAdapter
import com.binh.android.cookies.home.detail.viewmodel.PostDetailsViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class PostDetailsActivity : AppCompatActivity() {
    private val postDetailsViewModel by viewModels<PostDetailsViewModel> {
        PostDetailsViewModel.PostDetailsViewModelFactory(intent.extras?.getString(KEY_POST_ID)!!)
    }

    private lateinit var binding: ActivityPostDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_details)

        binding.lifecycleOwner = this

        binding.postContent.viewModel = postDetailsViewModel

        setUpToolbar()

        bind()

        checkAuth()

        setUpComments()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpComments() {
        val dataSource = FirebaseDatabase.getInstance().reference
        val commentQuery = dataSource.child("comments").orderByChild("postId")
            .equalTo(intent.extras?.getString(KEY_POST_ID)!!)

        val commentOptions: FirebaseRecyclerOptions<Comment> =
            FirebaseRecyclerOptions.Builder<Comment>()
                .setQuery(commentQuery, Comment::class.java)
                .setLifecycleOwner(this)
                .build()

        val commentAdapter = CommentAdapter(commentOptions)

        binding.postContent.commentRecyclerView.adapter = commentAdapter
    }

    private fun setUpObserver() {
        postDetailsViewModel.thisPostLiked.observe(this, {
            when (it) {
                true -> binding.toolbar.menu.findItem(R.id.action_like)
                    .setIcon(R.drawable.ic_like)
                false -> binding.toolbar.menu.findItem(R.id.action_like)
                    .setIcon(R.drawable.ic_unlike)
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun checkAuth() {
        postDetailsViewModel.isLoggedIn.observe(this, {
            binding.postContent.commentInputGroup.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun bind() {
        postDetailsViewModel.post.observe(this, { post ->
            Glide.with(binding.postImage)
                .load(post.photoUrl)
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                ).into(binding.postImage)
        })
    }

    private fun share() {
        val post = postDetailsViewModel.post.value ?: return
        val shareMsg = getString(R.string.share_link, post.postId)

        val intent = ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setText(shareMsg)
            .intent

        startActivity(Intent.createChooser(intent, null))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        postDetailsViewModel.isAdmin.observe(this, {
            binding.toolbar.menu.findItem(R.id.action_update).isVisible = it
            binding.toolbar.menu.findItem(R.id.action_like).isVisible = it
        })
        setUpObserver()
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                share()
                return true
            }
            R.id.action_update -> {
                onItemClicked(intent.extras?.getString(KEY_POST_ID)!!)
            }
            R.id.action_like -> {
                when (postDetailsViewModel.thisPostLiked.value) {
                    true -> postDetailsViewModel.postUnlike()
                    false -> postDetailsViewModel.postLiked()
                    else -> Toast.makeText(
                        applicationContext,
                        "Something went wrong!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val KEY_POST_ID = "postId"

        fun getStartIntent(
            context: Context,
            postId: String
        ) = Intent(context, PostDetailsActivity::class.java).apply { putExtra(KEY_POST_ID, postId) }
    }

    private fun onItemClicked(postId: String) {
        when (postId) {
            "" -> {
                Toast.makeText(this, "Unable to load detail!", Toast.LENGTH_LONG).show()
            }
            else -> {

                val intent = Intent(this, MainActivity::class.java).putExtra("EDIT_POST_ID", postId)
                    .putExtra("EDIT_POST", true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}