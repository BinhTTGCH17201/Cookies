package com.binh.android.cookies.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.binh.android.cookies.R
import com.binh.android.cookies.data.Post
import com.binh.android.cookies.databinding.ItemFoodBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions


class PostListAdapter(
    private val options: FirebaseRecyclerOptions<Post>,
    private val onItemClicked: (Post, ImageView) -> Unit
) :
    FirebaseRecyclerAdapter<Post, PostListAdapter.PostViewHolder>(options) {

    class PostViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post, onItemClicked: (Post, ImageView) -> Unit) {
            binding.postTitle.text = post.title
            Glide.with(binding.photo)
                .load(post.photoUrl.toUri())
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                ).into(binding.photo)

            binding.root.setOnClickListener {
                onItemClicked(post, binding.photo)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PostViewHolder(
        ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int,
        model: Post
    ) = holder.bind(getItem(position), onItemClicked)
}