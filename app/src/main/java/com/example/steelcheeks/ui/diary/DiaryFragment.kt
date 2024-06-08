package com.example.steelcheeks.ui.diary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.steelcheeks.SteelCheeksApplication
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.databinding.FragmentDiaryBinding
import com.google.android.material.datepicker.MaterialDatePicker
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter


class DiaryFragment : Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiaryViewModel by activityViewModels {
        DiaryViewModelFactory(
            (activity?.application as SteelCheeksApplication).database
        )
    }
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var datePicker: MaterialDatePicker<Long>

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

        diaryAdapter = DiaryAdapter {diaryEntry -> showDeleteConfirmationDialog(diaryEntry)}

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.recyclerView.adapter = diaryAdapter

        /* Date Picker */
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(viewModel.date.value?.plusDays(1)?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selection?.let {
                val selectedDate = Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                viewModel.setDate(selectedDate)
            }
        }

        binding.tvDate.setOnClickListener {
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }


        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )

        viewModel.diaryEntries.observe(viewLifecycleOwner) {entries ->
            entries?.let {
                diaryAdapter.submitList(it)
            }
        }

        // Update UI with the diary totals
        viewModel.diaryTotals.observe(viewLifecycleOwner) { totals ->
            totals?.let {
                binding.tvCaloriesValue.text = it.totalCalories?.toString() ?: "0"
                binding.tvCarbsValue.text = it.totalCarbohydrates?.toString() ?: "0"
                binding.tvProteinsValue.text = it.totalProteins?.toString() ?: "0"
                binding.tvFatsValue.text = it.totalFat?.toString() ?: "0"
            }
        }

        //Initial date
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        binding.tvDate.text = viewModel.date.value?.let { dateFormatter.format(it) }

        binding.btnBack.setOnClickListener {
            viewModel.date.value?.let {
                viewModel.setDate(it.minusDays(1))
            }
        }

        binding.btnForward.setOnClickListener {
            viewModel.date.value?.let {
                viewModel.setDate(it.plusDays(1))
            }
        }

        binding.fabAddItem.setOnClickListener {
            val date = viewModel.date.value?.toEpochDay()
            val action = DiaryFragmentDirections.actionDiaryFragmentToFoodListFragment(date ?: -1)
            findNavController().navigate(action)
        }

        // Observe the date and update UI accordingly
        viewModel.date.observe(viewLifecycleOwner) { date ->
            binding.tvDate.text = dateFormatter.format(date)
        }
    }

    private fun showDeleteConfirmationDialog(diaryEntry: DiaryEntryEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteDiaryEntry(diaryEntry)
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}