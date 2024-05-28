package com.example.steelcheeks.ui.food

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.steelcheeks.R
import com.example.steelcheeks.SteelCheeksApplication
import com.example.steelcheeks.databinding.FragmentFoodListBinding
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class FoodListFragment : Fragment(), MenuProvider {

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
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        _binding = FragmentFoodListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setLoadingStatusAsReady()

        val adapter = FoodListAdapter {
            val action =
                FoodListFragmentDirections.actionFoodListFragmentToFoodDetailFragment(it.code)
            findNavController().navigate(action)
        }

        // The lifecycle of the LiveData bound to the layout is that of the Fragment's
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.recyclerView.adapter = adapter
        // Gives the binding access to the FoodsViewModel
        binding.viewModel = viewModel

        /* viewModel.products.observe(viewLifecycleOwner) {foodList ->
            foodList?.let {
                val foodItems = it.products.map { food -> FoodItem.ResponseFoodItem(food) }
                adapter.submitList(foodItems)
            }
        }

        viewModel.filteredLocalFoodList.observe(viewLifecycleOwner) { filteredList ->
            filteredList?.let {
                adapter.submitList(foodItems)
            }
        } */

        viewModel.foodItems.observe(viewLifecycleOwner) {foodList ->
            foodList?.let {
                adapter.submitList(foodList)
            }
        }

        viewModel.localFoodList.observe(viewLifecycleOwner) {localFoodList ->
            localFoodList?.let {
                viewModel.setListToLocalFoodItems()
                adapter.submitList(viewModel.foodItems.value)
            }
        }


        //Search Field
        val searchEditText = binding.searchField.editText

        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (searchEditText?.text.toString() != query) {
                searchEditText?.setText(query)
            }
        }

        searchEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {     //Enter button is pressed
                val searchText = searchEditText.text.toString()
                viewModel.setSearchQuery(searchText)
                viewModel.getFoodEntries(searchText)
                return@setOnEditorActionListener true
            }
            false
        }

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                viewModel.setSearchQuery(searchText)
                if (searchText.isNotBlank()) {
                    viewModel.filterLocalFoodList(searchText)
                } else {
                    viewModel.filterLocalFoodList("")
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.food_list_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.scan_barcode -> {
                startBarcodeScanner()
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Added to prevent memory leaks
    }

    private fun startBarcodeScanner() {
        val options = ScanOptions()
        options.setPrompt("Scan a barcode")
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        barcodeLauncher.launch(options)
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val barcode = result.contents.toString()
            viewModel.getFoodByBarcode(barcode)
            viewModel.setLoadingStatusAsReady()     // Reset loading status
        }
    }
}