package com.example.reclaim.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.reclaim.bindImage
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.example.reclaim.databinding.HomeItemBinding

class HomeAdapter(val context: Context, val list: List<UserProfile>, val onClickListener: OnClickListener):
    RecyclerView.Adapter<HomeAdapter.DatingViewHolder>() {
    inner class DatingViewHolder(val binding: HomeItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: UserProfile){
            binding.executePendingBindings()
            binding.userNameTitle.text = item.userName
            binding.worriesDescription.setText(item.worriesDescription)
            binding.userTag.text = item.gender + "， 30"
            binding.aboutmeDescription.text = item.selfDescription

            binding.aboutmeTitle.visibility = View.GONE
            binding.aboutmeDescription.visibility = View.GONE

            binding.infoBtn.setOnClickListener {

                if (binding.aboutmeTitle.visibility == View.VISIBLE){

                    binding.seperateDot.visibility = View.GONE
                    binding.aboutmeTitle.visibility = View.GONE
                    binding.aboutmeDescription.visibility = View.GONE
                }else{
                    binding.seperateDot.visibility = View.VISIBLE
                    binding.aboutmeTitle.visibility = View.VISIBLE
                    binding.aboutmeDescription.visibility = View.VISIBLE
                }

            }

            if(item.worryType == UserManager.userType){
                binding.sameTypeTag.visibility = View.VISIBLE
                binding.sameTypeContainer.visibility = View.VISIBLE
            }else{
                binding.sameTypeTag.visibility = View.GONE
                binding.sameTypeContainer.visibility = View.GONE
            }
        }
    }

    class OnClickListener(val dislistener: (position: Int) -> Unit, val likeListener: (position: Int) -> Unit){
        fun onDisLike(position: Int) = dislistener(position)
        fun onLike(position: Int) = likeListener(position)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatingViewHolder {
        return DatingViewHolder(HomeItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DatingViewHolder, position: Int) {



        holder.bind(list[position])

        list[position].imageUri?.let { bindImage(holder.binding.usersFirstImg, it) }

        holder.binding.dislikeBtn.setOnClickListener {
            onClickListener.onDisLike(position)
        }

        holder.binding.likeBtn.setOnClickListener {
            onClickListener.onLike(position)
        }


    }
}