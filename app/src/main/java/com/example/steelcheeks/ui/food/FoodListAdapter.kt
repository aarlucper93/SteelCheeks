package com.example.steelcheeks.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.steelcheeks.data.database.food.FoodEntity
import com.example.steelcheeks.databinding.ListItemBinding
import com.example.steelcheeks.data.network.Product
import com.example.steelcheeks.domain.Food

class FoodListAdapter(private val onItemClicked: (Food) -> Unit) :
    ListAdapter<Food, FoodListAdapter.ViewHolder>(
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
        fun bind(Food: Food) {
            binding.apply {
                productName.text = Food.productName
                productBrand.text = Food.productBrands
                //TODO: Extract hardcoded strings to string resources (passing context from fragment as adapter's parameter?)
                productAmount.text = "${Food.productQuantity}${Food.productQuantityUnit} : "     //TODO P1: Quitar el hardcodeo
                productEnergy.text = "${Food.energyKcal.toString()} kcal"
            }
        }
    }

    companion object {
        private val Diffcallback = object : DiffUtil.ItemCallback<Food>() {
            override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem.code == newItem.code
            }

            override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem == newItem
            }
        }
    }
}