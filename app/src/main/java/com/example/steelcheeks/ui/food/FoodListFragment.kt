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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.steelcheeks.R
import com.example.steelcheeks.SteelCheeksApplication
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.databinding.FragmentFoodListBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.threeten.bp.LocalDate

class FoodListFragment : Fragment(), MenuProvider {

    private val viewModel: FoodsViewModel by activityViewModels {
        FoodsViewModelFactory(
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

        // Retrieve the date argument
        val entryDateEpochDay = arguments?.getLong("selectedDate", LocalDate.now().toEpochDay())
        viewModel.setDateForNewEntry(entryDateEpochDay)


        viewModel.setLoadingStatusAsReady()

        val adapter = FoodListAdapter (
            {food -> val action =
                FoodListFragmentDirections.actionFoodListFragmentToFoodDetailFragment(food.code)
                findNavController().navigate(action)
            },
            { food, isSelected ->
                viewModel.toggleItemSelected(food, isSelected)
            }

        )

        // The lifecycle of the LiveData bound to the layout is that of the Fragment's
        binding.lifecycleOwner = viewLifecycleOwner
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
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

        viewModel.foodItems.observe(viewLifecycleOwner) { foodList ->
            foodList?.let {
                adapter.submitList(foodList)
            }
        }

        /*viewModel.localFoodList.observe(viewLifecycleOwner) { localFoodList ->
            if (viewModel.remoteListMode.value == false) {
                localFoodList?.let {
                    viewModel.setListToLocalFoodItems()
                    //adapter.submitList(viewModel.foodItems.value)
                }
            }
        }*/

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            Snackbar.make(
                requireView(),
                message,
                Snackbar.LENGTH_SHORT
            ).show()
        }

        viewModel.selectedItems.observe(viewLifecycleOwner) {
            // Update action bar with the count and check icon if necessary
            activity?.invalidateOptionsMenu()
        }

        viewModel.remoteListMode.observe(viewLifecycleOwner) {
            viewModel.clearSelectedItems()
        }


        /* Search Field */
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
                viewModel.setRemoteListMode(true)
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
                    if (viewModel.remoteListMode.value == false) {
                        viewModel.filterLocalFoodList(searchText)
                    }
                } else {
                    viewModel.setRemoteListMode(false)
                    viewModel.setListToLocalFoodItems()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.clearSelectedItems()
    }


    /* Menú de la barra superior */
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.food_list_menu, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        val count = viewModel.selectedCount.value ?: 0
        val menuItem = menu.findItem(R.id.action_selected_count)

        if (viewModel.remoteListMode.value == true) {
            menuItem.icon = ContextCompat.getDrawable( requireContext(), R.drawable.ic_save_24)
        }
        menuItem.title = count.toString()
        menuItem.isVisible = count > 0
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.scan_barcode -> {
                startBarcodeScanner()
                true
            }
            R.id.action_selected_count -> {
                if (viewModel.remoteListMode.value == false) {
                    showConfirmationDialogForSelectedItems()
                    true
                } else {
                    viewModel.insertFoodListToLocalDatabase()
                    true
                }
            }

            else -> false
        }
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
            viewModel.getFoodByBarcode(barcode) {
                navigateToDetailScreen(it)
            }
            viewModel.setLoadingStatusAsReady()     // Reset loading status
        } else {
            Snackbar.make(
                requireView(),
                "No product found with this barcode",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigateToDetailScreen(barcode: String) {
        val action =
            FoodListFragmentDirections.actionFoodListFragmentToFoodDetailFragment(barcode)
        findNavController().navigate(action)
    }

    private fun showConfirmationDialogForSelectedItems() {
        val selectedItems = viewModel.selectedItems.value ?: return

        // Show confirmation dialog for each item in the selectedItems list
        for (food in selectedItems) {       //TODO: Cambiar para que aparezca una tras la otra y no todas de golpe
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_food, null)
            val textInput = dialogView.findViewById<TextInputEditText>(R.id.textInput)
            val tilFood = dialogView.findViewById<TextInputLayout>(R.id.textInputLayoutAddFood)
            tilFood.hint = "Serving size (${food.productQuantityUnit})"
            textInput.setText(food.productQuantity.toString())

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(food.productName)
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val quantity = textInput.text.toString().toLongOrNull() ?: food.productQuantity
                    val diaryEntry = DiaryEntryEntity(
                        foodCode = food.code,
                        date = viewModel.getDateForNewEntry(),
                        quantity = quantity,
                        productName = food.productName,
                        productBrands = food.productBrands,
                        productQuantityUnit = food.productQuantityUnit,
                        imageUrl = food.imageUrl,
                        energyKcal = food.energyKcal,
                        carbohydrates = food.carbohydrates,
                        proteins = food.proteins,
                        fat = food.fat
                    )
                    viewModel.insertDiaryEntry(diaryEntry)
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }
    }
}