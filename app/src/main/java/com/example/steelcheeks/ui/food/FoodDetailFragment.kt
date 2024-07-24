package com.example.steelcheeks.ui.food

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.steelcheeks.R
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.databinding.FragmentFoodDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class FoodDetailFragment : Fragment() {

    private var _binding: FragmentFoodDetailBinding? = null
    private val binding get() = _binding!!
    private val args: FoodDetailFragmentArgs by navArgs()
    private val viewModel: FoodsViewModel by activityViewModels()
    /*private val viewModel: FoodsViewModel by activityViewModels {
        FoodsViewModelFactory (
            (activity?.application as SteelCheeksApplication).database
        )
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFoodDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (viewModel.itemWasScanned.value == false) {
            viewModel.setFoodItemByBarcode(args.barcode)
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.setScannedItemAsLoaded()

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            Snackbar.make(
                requireView(),
                message,
                Snackbar.LENGTH_SHORT
            ).show()
        }

        binding.fabSaveToDatabase.setOnClickListener {
            if (viewModel.food.value!!.isFromLocal) {
                showConfirmationDialog()
            } else {
                viewModel.insertFoodToLocalDatabase(viewModel.food)
                findNavController().popBackStack()
            }
        }
    }


    private fun showConfirmationDialog() {

        val food = viewModel.food.value
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_food, null)
        val textInput = dialogView.findViewById<TextInputEditText>(R.id.textInput)
        val tilFood = dialogView.findViewById<TextInputLayout>(R.id.textInputLayoutAddFood)
        tilFood.hint = "Serving size (${food?.productQuantityUnit})"
        textInput.setText(food?.productQuantity.toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(food?.productName)
            .setView(dialogView)
            .setPositiveButton("Add") {_, _ ->

                val quantity = textInput.text.toString().toLongOrNull() ?: food?.productQuantity ?: 100
                val diaryEntry = DiaryEntryEntity(
                    foodCode = food?.code ?: "",
                    date = viewModel.getDateForNewEntry(),
                    quantity = quantity,
                    productName = food?.productName ?: "",
                    productBrands = food?.productBrands,
                    productQuantityUnit = food?.productQuantityUnit,
                    imageUrl = food?.imageUrl,
                    energyKcal = food?.energyKcal,
                    carbohydrates = food?.carbohydrates,
                    proteins = food?.proteins,
                    fat = food?.fat
                )
                viewModel.insertDiaryEntry(diaryEntry)
                Snackbar.make(requireView(), "Food added to diary", Snackbar.LENGTH_SHORT).show()
                val action =
                    FoodDetailFragmentDirections.actionFoodDetailFragmentToDiaryFragment()
                findNavController().navigate(action)

            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}