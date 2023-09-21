package com.example.reclaim.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.reclaim.R
import com.example.reclaim.databinding.PagerImageItemBinding

private const val TAG = "IMAGEADAPTER"
class ImageAdapter:ListAdapter<Uri,ImageAdapter.ImageViewHolder>(DiffCallback){
    class ImageViewHolder(private val binding: PagerImageItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(imageUri: Uri){
            binding.imageUri = imageUri.toString()
            Log.i(TAG, imageUri.toString())
            binding.executePendingBindings()
        }

    }


    companion object DiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            PagerImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image)
    }
}