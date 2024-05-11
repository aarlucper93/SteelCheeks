package com.example.steelcheeks.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.steelcheeks.SteelCheeksApplication
import com.example.steelcheeks.databinding.FragmentFoodDetailBinding
import com.google.android.material.snackbar.Snackbar


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

            Glide.with(requireContext())
                .load(viewModel.food.value?.imageUrl)
                .into(foodDetailImage)
        }

        Log.d("FoodDetailFragment", "imageUrl: ${viewModel.food.value?.imageUrl}")

        binding.fabSaveToDatabase.setOnClickListener {
            viewModel.insertFoodToLocalDatabase(viewModel.food)
            if (viewModel.result.value != -1L){
                Snackbar.make(
                    this.requireView(),
                    "Food saved to the local database",
                    Snackbar.LENGTH_SHORT
                ).show()

            }
        }
    }
}