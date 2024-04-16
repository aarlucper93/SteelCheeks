package com.example.steelcheeks.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.steelcheeks.R
import com.example.steelcheeks.databinding.FragmentFoodListBinding
import com.example.steelcheeks.network.Food

class FoodListFragment : Fragment() {

    private val viewModel: FoodsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFoodListBinding.inflate(inflater)

        // Link the lifecycle of the LiveData bound to the layout with the Fragment's lifecycle
        binding.lifecycleOwner = this
        // Gives the binding access to the FoodsViewModel
        binding.viewModel = viewModel

        return binding.root
    }
}