package com.example.reclaim.editprofile

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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