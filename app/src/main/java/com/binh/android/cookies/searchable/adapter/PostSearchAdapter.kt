package com.binh.android.cookies.searchable.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import com.binh.android.cookies.R
import com.binh.android.cookies.data.PostSearched
import com.binh.android.cookies.databinding.ItemSearchBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class PostSearchAdapter(
    private val onItemClicked: (PostSearched, ImageView) -> Unit
) :
    PagedListAdapter<PostSearched, PostSearchAdapter.PostViewHolder>(PostSearchAdapter) {

    class PostViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: PostSearched, onItemClicked: (PostSearched, ImageView) -> Unit) {
            binding.postTitleSearch.text = post.highlightedTitle?.toSpannedString() ?: post.title
            Glide.with(binding.photoSearch)
                .load(post.photoUrl.toUri())
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                ).into(binding.photoSearch)

            binding.root.setOnClickListener {
                onItemClicked(post, binding.photoSearch)
            }

            binding.executePendingBindings()
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
        holder: PostViewHolder,
        position: Int
    ) {
        val post = getItem(position)

        if (post != null) holder.bind(post, onItemClicked)
    }

    companion object : DiffUtil.ItemCallback<PostSearched>() {

        override fun areItemsTheSame(
            oldItem: PostSearched,
            newItem: PostSearched
        ): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: PostSearched,
            newItem: PostSearched
        ): Boolean {
            return oldItem == newItem
        }
    }
}