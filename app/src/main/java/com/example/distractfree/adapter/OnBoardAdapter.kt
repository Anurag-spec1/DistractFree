package com.example.distractfree.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.distractfree.dataclasses.OnBoardData
import com.example.distractfree.databinding.OnboardItemLayoutBinding

class OnBoardAdapter(
    var context: Context,
    var dataList: ArrayList<OnBoardData>,
) : RecyclerView.Adapter<OnBoardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OnboardItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.animationView.setAnimation(dataList[position].image)
        holder.binding.descriptionTxt.text = dataList[position].description
        holder.binding.headTxt1.text = dataList[position].head1
        holder.binding.headPTxt2.text = dataList[position].head2


    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(var binding: OnboardItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}