package com.binh.android.cookies.utils

import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.binh.android.cookies.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("accountImageUrl")
fun accountImageUrl(photoImage: ImageView, imageUrl: Uri?) {
    if (imageUrl != null) {
        Glide.with(photoImage)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_account_default)
            ).into(photoImage)
    } else {
        Glide.with(photoImage)
            .load(R.drawable.ic_account_default)
            .into(photoImage)
    }
}

@BindingAdapter("postImageUrl")
fun postImageUrl(photoImage: ImageView, imageUrl: Any?) {
    if (imageUrl != null) {
        when (imageUrl) {
            String -> imageUrl.toString().toUri()
        }
        Glide.with(photoImage)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            ).into(photoImage)
    } else {
        Glide.with(photoImage)
            .load(R.drawable.choose_image_placeholder)
            .into(photoImage)
    }
}