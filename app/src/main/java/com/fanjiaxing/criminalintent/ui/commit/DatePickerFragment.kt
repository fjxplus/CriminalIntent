package com.fanjiaxing.criminalintent.ui.commit

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val REQUEST_DATE = "RequestDate"

private const val RESULT_DATE = "SelectedDate"

class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable("ARG_DATE") as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dateListener = DatePickerDialog.OnDateSetListener{
            _: DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date = GregorianCalendar(year, month, day).time
            val result = Bundle().apply {
                putSerializable(RESULT_DATE, resultDate)
            }
            parentFragmentManager.setFragmentResult(REQUEST_DATE, result)
        }
        return DatePickerDialog(requireContext(), dateListener, initialYear, initialMonth, initialDay)
    }

    companion object{
        fun newInstance(date: Date): DatePickerFragment{
            val args = Bundle().apply {
                putSerializable("ARG_DATE", date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }

}