package com.binh.android.cookies.searchable.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.binh.android.cookies.R
import com.binh.android.cookies.data.Post
import com.binh.android.cookies.databinding.ItemSearchBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class PostSearchAdapter(
    private val options: FirebaseRecyclerOptions<Post>,
    private val onItemClicked: (Post, ImageView) -> Unit
) :
    FirebaseRecyclerAdapter<Post, PostSearchAdapter.PostViewHolder>(options) {
    class PostViewHolder(private val binding: ItemSearchBinding) :
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
        ItemSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(
        holder: PostSearchAdapter.PostViewHolder,
        position: Int,
        model: Post
    ) = holder.bind(getItem(position), onItemClicked)

}