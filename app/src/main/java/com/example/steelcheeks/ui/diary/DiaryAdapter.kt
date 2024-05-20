package com.example.steelcheeks.ui.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.databinding.ListItemBinding

class DiaryAdapter() : ListAdapter<DiaryEntryEntity, DiaryAdapter.ViewHolder>(Diffcallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(private var binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(diaryEntry: DiaryEntryEntity) {
            binding.apply {
                binding.productName.text = diaryEntry.foodCode
                binding.productAmount.text = diaryEntry.quantity.toString()
            }
        }
    }

    companion object {
        private val Diffcallback = object : DiffUtil.ItemCallback<DiaryEntryEntity>() {
            override fun areItemsTheSame(
                oldItem: DiaryEntryEntity,
                newItem: DiaryEntryEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: DiaryEntryEntity,
                newItem: DiaryEntryEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}