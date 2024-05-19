package com.example.steelcheeks.ui.diary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.steelcheeks.SteelCheeksApplication
import com.example.steelcheeks.databinding.FragmentDiaryBinding
import org.threeten.bp.format.DateTimeFormatter


class DiaryFragment : Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiaryViewModel by activityViewModels {
        DiaryViewModelFactory(
            (activity?.application as SteelCheeksApplication).database
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        // Update UI with the diary totals
        viewModel.diaryTotals.observe(viewLifecycleOwner) { totals ->
            totals?.let {
                binding.tvCaloriesValue.text = it.totalCalories?.toString() ?: "0"
                binding.tvCarbsValue.text = it.totalCarbohydrates?.toString() ?: "0"
                binding.tvProteinsValue.text = it.totalProteins?.toString() ?: "0"
                binding.tvFatsValue.text = it.totalFat?.toString() ?: "0"
            }
        }

        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        binding.tvDate.text = viewModel.date.value?.let { dateFormatter.format(it) }

    }
}