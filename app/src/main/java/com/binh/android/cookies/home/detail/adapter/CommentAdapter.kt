package com.binh.android.cookies.home.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.binh.android.cookies.R
import com.binh.android.cookies.data.Comment
import com.binh.android.cookies.data.User
import com.binh.android.cookies.databinding.ItemCommentBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CommentAdapter(options: FirebaseRecyclerOptions<Comment>) :
    FirebaseRecyclerAdapter<Comment, CommentAdapter.CommentViewHolder>(options) {

    class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            getUserName(comment.userId)
            binding.comment.text = comment.content
            checkCommentOwner(comment)
        }

        private fun getUserName(uid: String) {
            FirebaseDatabase.getInstance().reference.child("users/$uid").get()
                .addOnSuccessListener {
                    val user = it.getValue(User::class.java)
                    binding.username.text = user?.name ?: ""
                    Glide.with(binding.userPhoto)
                        .load(user?.photo)
                        .apply(
                            RequestOptions()
                                .circleCrop()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.ic_account_default)
                        ).into(binding.userPhoto)
                }
        }

        private fun checkCommentOwner(comment: Comment) {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUid == comment.userId) {
                binding.deleteComment.visibility = View.VISIBLE
                binding.deleteComment.setOnClickListener { view ->
                    FirebaseDatabase.getInstance().reference.child("comments/${comment.commentId}")
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) Toast.makeText(
                                view.context,
                                "Successfully delete comment",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CommentViewHolder(
            ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: Comment) =
        holder.bind(getItem(position))
}