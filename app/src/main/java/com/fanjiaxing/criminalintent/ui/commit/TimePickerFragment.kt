package com.fanjiaxing.criminalintent.ui.commit

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val REQUEST_DATE = "RequestDate"

private const val RESULT_TIME = "SelectedTime"

class TimePickerFragment: DialogFragment() {

    companion object{
        fun newInstance(date: Date): TimePickerFragment{
            val args = Bundle().apply {
                putSerializable("ARG_KEY", date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val data = arguments?.getSerializable("ARG_KEY") as Date
        val calendar = Calendar.getInstance()
        calendar.time = data
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)
        val timeListener = TimePickerDialog.OnTimeSetListener{
                _: TimePicker, hour, minute ->
            val resultTime = GregorianCalendar(0, 0 ,0, hour, minute).time
            val result = Bundle().apply {
                putSerializable(RESULT_TIME, resultTime)
            }
            parentFragmentManager.setFragmentResult(REQUEST_DATE, result)
        }
        return TimePickerDialog(requireContext(), timeListener, initialHour, initialMinute,true)
    }

}