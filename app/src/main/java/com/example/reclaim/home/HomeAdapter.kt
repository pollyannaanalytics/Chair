package com.example.reclaim.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.reclaim.bindImage
import com.example.reclaim.data.UserProfile
import com.example.reclaim.databinding.HomeItemBinding

class HomeAdapter(val context: Context, val list: List<UserProfile>):
    RecyclerView.Adapter<HomeAdapter.DatingViewHolder>() {
    inner class DatingViewHolder(val binding: HomeItemBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatingViewHolder {
        return DatingViewHolder(HomeItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DatingViewHolder, position: Int) {
        holder.binding.userNameTitle.text = list[position].userName
        holder.binding.worryType.text = list[position].worryType
        holder.binding.worriesDescription.setText(list[position].worriesDescription)

        list[position].imageUri?.let { bindImage(holder.binding.usersFirstImg, it) }
    }

}