package com.binh.android.cookies.home.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.binh.android.cookies.MainActivity
import com.binh.android.cookies.R
import com.binh.android.cookies.data.Comment
import com.binh.android.cookies.data.User
import com.binh.android.cookies.databinding.ActivityPostDetailsBinding
import com.binh.android.cookies.home.detail.adapter.CommentAdapter
import com.binh.android.cookies.home.detail.viewmodel.PostDetailsViewModel
import com.binh.android.cookies.home.detail.viewmodel.PostDetailsViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PostDetailsActivity : AppCompatActivity() {
    private lateinit var postDetailsViewModel: PostDetailsViewModel

    private lateinit var binding: ActivityPostDetailsBinding

    private lateinit var firebaseAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_details)

        val dataSource = FirebaseDatabase.getInstance().reference

        binding.lifecycleOwner = this

        val viewModelFactory =
            PostDetailsViewModelFactory(intent.extras?.getString(KEY_POST_ID)!!, application)

        postDetailsViewModel =
            ViewModelProvider(this, viewModelFactory).get(PostDetailsViewModel::class.java)

        binding.postContent.viewModel = postDetailsViewModel

        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bind()

        checkAuth()

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

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuthStateListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseAuth.getInstance().removeAuthStateListener(firebaseAuthStateListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun checkAuth() {
        firebaseAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val commentInputGroupView = binding.postContent.commentInputGroup
            if (user != null) commentInputGroupView.visibility = LinearLayout.VISIBLE
            else commentInputGroupView.visibility = LinearLayout.GONE
        }
    }

    private fun bind() {
        postDetailsViewModel.post.observe(this, { post ->
            binding.postContent.apply {
                postTitle.text = post.title
                postIngredient.text = post.ingredient
                postTimePeople.text = getString(
                    R.string.time_people_text,
                    post.people.toString(),
                    post.time.toString()
                )
                postPreparation.text = post.preparation
            }
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
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).get()
                .addOnSuccessListener { task ->
                    val userDb = task.getValue(User::class.java)
                    binding.toolbar.menu.findItem(R.id.action_update).isVisible = userDb?.admin!!
                }
        } else binding.toolbar.menu.findItem(R.id.action_update).isVisible = false
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
                startActivity(intent)
            }
        }
    }
}