package com.example.reclaim

import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUri")
fun bindImage(imageView: ImageView, imgUri: String){
    imgUri?.let {
        val imgUri = it.toUri()
        try {
            Glide.with(imageView.context)
                .load(imgUri)
                .into(imageView)
            Log.e("glide", "deal successfully")
        }catch (e: Exception){
            Log.e("glide", "${e}")
        }

    }
}