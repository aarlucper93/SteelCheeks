package com.example.steelcheeks.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.steelcheeks.databinding.ListItemBinding
import com.example.steelcheeks.network.Food

class FoodListAdapter(private val onItemClicked: (Food) -> Unit) : ListAdapter<Food, FoodListAdapter.ViewHolder>(Diffcallback) {
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
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
        holder.bind(item)
    }

    class ViewHolder(private var binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: Food) {
            binding.apply {
                productName.text = food.productName
                productBrand.text = food.productBrands
                //TODO: Extract hardcoded strings to string resources (passing context from fragment as adapter's parameter?)
                productEnergy.text = "Calories: ${food.nutriments.energyKcal}"
                productCarbohydrates.text = "Carbs: ${food.nutriments.carbohydrates}g"
                productProteins.text = "Proteins: ${food.nutriments.proteins}g"

            }
        }
    }

    companion object {
        private val Diffcallback = object : DiffUtil.ItemCallback<Food>() {
            override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem.code == newItem.code
            }

        }
    }


}