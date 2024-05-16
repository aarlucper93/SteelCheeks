package com.example.steelcheeks.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.steelcheeks.SteelCheeksApplication
import com.example.steelcheeks.databinding.FragmentFoodListBinding

class FoodListFragment : Fragment() {

    private val viewModel: FoodsViewModel by activityViewModels {
        FoodsViewModelFactory (
            (activity?.application as SteelCheeksApplication).database
        )
    }
    private var _binding: FragmentFoodListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setLoadingStatusAsReady()

        val adapter = FoodListAdapter{
            when (it) {
                is FoodItem.ResponseFoodItem -> {
                    viewModel.isLocalLoad = false
                    val action = FoodListFragmentDirections.actionFoodListFragmentToFoodDetailFragment(it.food.code)
                    findNavController().navigate(action)
                }
                is FoodItem.LocalFoodItem -> {
                    viewModel.isLocalLoad = true
                    val action = FoodListFragmentDirections.actionFoodListFragmentToFoodDetailFragment(it.food.code)
                    findNavController().navigate(action)
                }
            }
        }

        // The lifecycle of the LiveData bound to the layout is that of the Fragment's
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.recyclerView.adapter = adapter
        // Gives the binding access to the FoodsViewModel
        binding.viewModel = viewModel

        viewModel.products.observe(viewLifecycleOwner) {foodList ->
            foodList?.let {
                val foodItems = it.products.map { food -> FoodItem.ResponseFoodItem(food) }
                adapter.submitList(foodItems)
            }
        }

        viewModel.localFoodList.observe(viewLifecycleOwner) {localFoodList ->
            localFoodList?.let {
                val foodItems = it.map { food -> FoodItem.LocalFoodItem(food) }
                adapter.submitList(foodItems)
            }
        }

        viewModel.filteredLocalFoodList.observe(viewLifecycleOwner) { filteredList ->
            filteredList?.let {
                val foodItems = it.map { food -> FoodItem.LocalFoodItem(food) }
                adapter.submitList(foodItems)
            }
        }


        val searchEditText = binding.searchField.editText
        searchEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {     //Enter button is pressed
                val searchText = searchEditText.text.toString()
                viewModel.getFoodEntries(searchText)
                return@setOnEditorActionListener true
            }
            false
        }

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (searchText.isNotBlank()) {
                    viewModel.filterLocalFoodList(searchText)
                } else {
                    viewModel.filterLocalFoodList("")
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        //adapter.submitList(viewModel.products.value?.products)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Added to prevent memory leaks
    }
}