package me.moty.cylost.ui.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import me.moty.cylost.RecordType
import me.moty.cylost.databinding.FragmentFilterBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.stream.Collectors

class FilterFragment(private val frag: HomeFragment) : DialogFragment() {
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val options = enumValues<RecordType>().toMutableList().stream().map {
            it.display
        }.collect(Collectors.toList())
        options.add(0, "選擇物品類型")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val typeSpinner = binding.typeSpinner
        val from = binding.fromDate
        val to = binding.toDate
        val clearBtn = binding.btnClear
        val applyBtn = binding.btnApply

        from.onFocusChangeListener = focusListener(from, to, true)
        to.onFocusChangeListener = focusListener(to, from, false)
        typeSpinner.adapter = adapter
        var selectedType: RecordType? = null

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0)
                    selectedType = null
                else
                    selectedType = RecordType.values()[position - 1]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        applyBtn.setOnClickListener {
            if (from.text.isNotEmpty() && to.text.isNotEmpty())
                frag.condition[FilterTag.DATE] =
                    "date BETWEEN '" + from.text.toString() + "' AND '" + to.text.toString() + "'"
            else if (frag.condition.containsKey(FilterTag.DATE))
                frag.condition.remove(FilterTag.DATE)
            if (selectedType != null)
                frag.condition[FilterTag.TYPE] = "type = '" + selectedType!!.name + "'"
            else if (frag.condition.containsKey(FilterTag.TYPE))
                frag.condition.remove(FilterTag.TYPE)
            frag.updateAdapterOutside()
            frag.page = 1
            dismiss()
        }
        clearBtn.setOnClickListener {
            from.text.clear()
            to.text.clear()
            typeSpinner.setSelection(0)
        }
        return root
    }

    fun focusListener(et: EditText, another: EditText, from: Boolean): OnFocusChangeListener {
        val calendar = Calendar.getInstance(Locale.TAIWAN)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN)
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            if (another.text.isNotEmpty() && (if (from) sdf.parse(another.text.toString())!!.time < calendar.time.time else sdf.parse(
                    another.text.toString()
                )!!.time > calendar.time.time)
            ) {
                et.text = another.text
                another.setText(sdf.format(calendar.time))
            } else
                et.setText(sdf.format(calendar.time))

        }

        return object : OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (!hasFocus) return
                v.clearFocus()
                DatePickerDialog(
                    requireContext(),
                    datePicker,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

        }
    }
}