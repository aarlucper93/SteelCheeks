package com.example.steelcheeks.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.steelcheeks.databinding.ListItemBinding
import com.example.steelcheeks.domain.Food

class FoodListAdapter(
    private val onItemClicked: (Food) -> Unit,
    private val onItemCheckedChanged: (Food, Boolean) -> Unit
) :
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
        holder.bind(item, onItemClicked, onItemCheckedChanged)
    }

    class ViewHolder(private var binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            food: Food,
            onItemClicked: (Food) -> Unit,
            onItemCheckedChanged: (Food, Boolean) -> Unit
        ) {
            itemView.setOnClickListener { onItemClicked(food) }
            binding.apply {
                productName.text = food.productName
                productBrand.text = food.productBrands
                //TODO: Extract hardcoded strings to string resources (passing context from fragment as adapter's parameter?)
                productAmount.text = "${food.productQuantity}${food.productQuantityUnit} : "
                productEnergy.text = "${food.energyKcal.toString()} kcal"

                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = food.isSelected
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onItemCheckedChanged(food, isChecked)
                }

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