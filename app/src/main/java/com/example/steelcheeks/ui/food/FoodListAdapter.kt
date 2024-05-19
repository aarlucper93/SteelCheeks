package com.example.steelcheeks.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.steelcheeks.data.database.food.FoodEntity
import com.example.steelcheeks.databinding.ListItemBinding
import com.example.steelcheeks.data.network.Food

sealed class FoodItem {
    data class ResponseFoodItem(val food: Food) : FoodItem()
    data class LocalFoodItem(val food: FoodEntity) : FoodItem()
}

class FoodListAdapter(private val onItemClicked: (FoodItem) -> Unit) : ListAdapter<FoodItem, FoodListAdapter.ViewHolder>(
    Diffcallback
) {
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
        fun bind(foodItem: FoodItem) {
            when (foodItem) {
                is FoodItem.ResponseFoodItem -> {
                    binding.apply {
                        productName.text = foodItem.food.productName
                        productBrand.text = foodItem.food.productBrands
                        //TODO: Extract hardcoded strings to string resources (passing context from fragment as adapter's parameter?)
                        productAmount.text = "${foodItem.food.productQuantity}${foodItem.food.productQuantityUnit} : "     //TODO P1: Quitar el hardcodeo
                        productEnergy.text = "${foodItem.food.nutriments.energyKcal.toString()} kcal"
                    }
                }
                is FoodItem.LocalFoodItem -> {
                    binding.apply {
                        productName.text = foodItem.food.productName
                        productBrand.text = foodItem.food.productBrands
                        //TODO: Extract hardcoded strings to string resources (passing context from fragment as adapter's parameter?)
                        productAmount.text = "${foodItem.food.productQuantity}${foodItem.food.productQuantityUnit} : "     //TODO P1: Quitar el hardcodeo
                        productEnergy.text = "${foodItem.food.energyKcal.toString()} kcal"

                    }
                }

                else -> {
                    throw Exception ("You fucked up")
                }
            }



        }
    }

    companion object {
        private val Diffcallback = object : DiffUtil.ItemCallback<FoodItem>() {
            override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
                return when {
                    oldItem is FoodItem.ResponseFoodItem && newItem is FoodItem.ResponseFoodItem ->
                        oldItem.food.code == newItem.food.code
                    oldItem is FoodItem.LocalFoodItem && newItem is FoodItem.LocalFoodItem ->
                        oldItem.food.code == newItem.food.code
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
                return oldItem == newItem
            }

        }
    }
}