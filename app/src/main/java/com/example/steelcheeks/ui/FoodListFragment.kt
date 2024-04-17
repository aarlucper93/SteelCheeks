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
    private var _binding: FragmentFoodListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFoodListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FoodListAdapter()

        // The lifecycle of the LiveData bound to the layout is that of the Fragment's
        binding.lifecycleOwner = viewLifecycleOwner
        binding.recyclerView.adapter = adapter
        // Gives the binding access to the FoodsViewModel
        binding.viewModel = viewModel
        viewModel.products.observe(viewLifecycleOwner) {entries ->
            entries?.let {
                adapter.submitList(it.products)
            }
        }
        adapter.submitList(viewModel.products.value?.products)


    }
}