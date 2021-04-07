package com.binh.android.cookies.home.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.binh.android.cookies.R
import com.binh.android.cookies.data.Comment
import com.binh.android.cookies.databinding.ActivityPostDetailsBinding
import com.binh.android.cookies.home.detail.adapter.CommentAdapter
import com.binh.android.cookies.home.detail.viewmodel.PostDetailsViewModel
import com.binh.android.cookies.home.detail.viewmodel.PostDetailsViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class PostDetailsActivity : AppCompatActivity() {
    private lateinit var postDetailsViewModel: PostDetailsViewModel

    private lateinit var binding: ActivityPostDetailsBinding

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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bind()

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

    private fun bind() {
        postDetailsViewModel.post.observe(this, Observer { post ->
            binding.postContent.apply {
                postTitle.text = post.title
                postIngredient.text = post.ingredient
                postTimePeople.text =
                    "for ${post.people} rations with ${post.time} minutes of cooking"
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

//    private fun share() {
//        val post = postDetailsViewModel.post.value ?: return
//        val shareMsg = getString(R.string.share_message, post.title, post.author)
//
//        val intent = ShareCompat.IntentBuilder.from(this)
//            .setType("text/plain")
//            .setText(shareMsg)
//            .intent
//
//        startActivity(Intent.createChooser(intent, null))
//    }


    companion object {
        private const val KEY_POST_ID = "postId"

        fun getStartIntent(
            context: Context,
            postId: String
        ) = Intent(context, PostDetailsActivity::class.java).apply { putExtra(KEY_POST_ID, postId) }
    }
}