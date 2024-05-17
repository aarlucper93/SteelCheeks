package com.example.steelcheeks.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.steelcheeks.R
import com.example.steelcheeks.SteelCheeksApplication
import com.example.steelcheeks.databinding.FragmentFoodDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class FoodDetailFragment : Fragment() {

    private var _binding: FragmentFoodDetailBinding? = null
    private val binding get() = _binding!!
    private val args: FoodDetailFragmentArgs by navArgs()
    private val viewModel: FoodsViewModel by activityViewModels {
        FoodsViewModelFactory (
            (activity?.application as SteelCheeksApplication).database
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        viewModel.setFoodItemByBarcode(args.barcode)
        binding.apply {
            tvBarcodeValue.text = viewModel.food.value?.code
            tvNameValue.text = viewModel.food.value?.productName
            tvBrandValue.text = viewModel.food.value?.productBrands
            tvServingSizeValue.text = viewModel.food.value?.productQuantity.toString()
            tvServingUnitValue.text = viewModel.food.value?.productQuantityUnit
            tvCaloriesValue.text = viewModel.food.value?.nutriments?.energyKcal.toString()
            tvProteinValue.text = viewModel.food.value?.nutriments?.proteins.toString()
            tvCarbsValue.text = viewModel.food.value?.nutriments?.carbohydrates.toString()
            tvFatValue.text = viewModel.food.value?.nutriments?.fat.toString()
            if (viewModel.isLocalLoad) {
                fabSaveToDatabase.setImageResource(R.drawable.ic_add_24)
            }

            Glide.with(requireContext())
                .load(viewModel.food.value?.imageUrl)
                .into(foodDetailImage)
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            Snackbar.make(
                requireView(),
                message,
                Snackbar.LENGTH_SHORT
            ).show()
        }

        binding.fabSaveToDatabase.setOnClickListener {
            if (viewModel.isLocalLoad) {
                showConfirmationDialog()
            } else {
                viewModel.insertFoodToLocalDatabase(viewModel.food)
                /*val action = FoodDetailFragmentDirections.actionFoodDetailFragmentToFoodListFragment()
                findNavController().navigate(action)*/
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
                //TODO: Save food to diary table.
                Snackbar.make(
                    requireView(),
                    "Food added to diary",
                    Snackbar.LENGTH_SHORT
                ).show()
                val action = FoodDetailFragmentDirections.actionFoodDetailFragmentToDiaryFragment()
                findNavController().navigate(action)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}