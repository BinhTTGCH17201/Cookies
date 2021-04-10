package com.binh.android.cookies.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.binh.android.cookies.R
import com.binh.android.cookies.data.Post
import com.binh.android.cookies.databinding.FragmentPostListBinding
import com.binh.android.cookies.home.detail.PostDetailsActivity
import com.binh.android.cookies.home.viewmodel.PostListViewModel
import com.binh.android.cookies.home.viewmodel.PostListViewModelFactory
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class PostListFragment : Fragment() {

    private lateinit var postListViewModel: PostListViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPostListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_post_list, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Cookies"

        val application = requireNotNull(this.activity).application

        val dataSource = FirebaseDatabase.getInstance()

        val viewModelFactory = PostListViewModelFactory(dataSource, application)

        postListViewModel =
            ViewModelProvider(this, viewModelFactory).get(PostListViewModel::class.java)

        binding.viewModel = postListViewModel

        binding.lifecycleOwner = this

        // Setup RecyclerView for FirebaseUI
        val easyQuery = dataSource.reference.child("posts").orderByChild("type").equalTo("Easy")
        val dailyQuery = dataSource.reference.child("posts").orderByChild("type").equalTo("Daily")
        val occasionsQuery =
            dataSource.reference.child("posts").orderByChild("type").equalTo("Occasions")
        val healthyQuery =
            dataSource.reference.child("posts").orderByChild("type").equalTo("Healthy")


        val easyTypeOptions: FirebaseRecyclerOptions<Post> = FirebaseRecyclerOptions.Builder<Post>()
            .setQuery(easyQuery, Post::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        val dailyTypeOptions: FirebaseRecyclerOptions<Post> =
            FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(dailyQuery, Post::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()

        val occasionsTypeOptions: FirebaseRecyclerOptions<Post> =
            FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(occasionsQuery, Post::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()

        val healthyTypeOptions: FirebaseRecyclerOptions<Post> =
            FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(healthyQuery, Post::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()

        val easyTypeAdapter = PostListAdapter(easyTypeOptions, this::onItemClicked)
        val dailyTypeAdapter = PostListAdapter(dailyTypeOptions, this::onItemClicked)
        val occasionsTypeAdapter = PostListAdapter(occasionsTypeOptions, this::onItemClicked)
        val healthyTypeAdapter = PostListAdapter(healthyTypeOptions, this::onItemClicked)

        binding.postListDaily.adapter = dailyTypeAdapter
        binding.postListOccasions.adapter = occasionsTypeAdapter
        binding.postListHealthy.adapter = healthyTypeAdapter
        binding.postListEasy.adapter = easyTypeAdapter

        return binding.root
    }

    private fun onItemClicked(post: Post, photo: ImageView) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            photo,
            photo.transitionName
        )

        when (post.postId) {
            "" -> {
                Toast.makeText(context, "Unable to load detail!", Toast.LENGTH_LONG).show()
            }
            else -> {
                val postId = post.postId

                val intent = PostDetailsActivity.getStartIntent(requireContext(), postId)
                startActivity(intent, options.toBundle())
            }
        }
    }
}