package com.example.steelcheeks.ui.diary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.databinding.ListItemDiaryBinding

class DiaryAdapter(private val onLongClickListener: (DiaryEntryEntity) -> Unit) : ListAdapter<DiaryEntryEntity, DiaryAdapter.ViewHolder>(Diffcallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemDiaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onLongClickListener)
    }

    class ViewHolder(private var binding: ListItemDiaryBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")        //Se utilizar string resources para evitar pasarle el contexto al adapter
        fun bind(diaryEntry: DiaryEntryEntity, onLongClickListener: (DiaryEntryEntity) -> Unit) {
            binding.apply {
                binding.productName.text = diaryEntry.productName
                binding.productBrand.text = diaryEntry.productBrands
                binding.productAmount.text = "${diaryEntry.quantity}${diaryEntry.productQuantityUnit}"
                binding.productEnergy.text ="${diaryEntry.energyKcal?.times(diaryEntry.quantity)?.div(100)?.toInt()}kcal"
            }

            binding.root.setOnLongClickListener {
                onLongClickListener(diaryEntry)
                true
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