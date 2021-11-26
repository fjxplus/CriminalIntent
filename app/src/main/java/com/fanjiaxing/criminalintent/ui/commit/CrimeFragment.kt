package com.fanjiaxing.criminalintent.ui.commit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fanjiaxing.criminalintent.R
import com.fanjiaxing.criminalintent.databinding.FragmentCrimeBinding
import com.fanjiaxing.criminalintent.ui.getScaledBitmap
import java.text.SimpleDateFormat
import java.util.*

private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = "RequestDate"
private const val RESULT_DATE = "SelectedDate"
private const val RESULT_TIME = "SelectedTime"
private const val DATE_FORMAT = "yyyy-MM-dd kk:mm"
private const val CALENDAR_FORMAT = "yyyy-MM-dd"
private const val TIME_FORMAT = "kk:mm"

class CrimeFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this).get(CrimeViewModel::class.java) }

    private var _binding: FragmentCrimeBinding? = null

    private val binding get() = _binding!!

    private lateinit var requestContactActivityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var captureActivityResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable("crime_id", crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crimeId: UUID = arguments?.getSerializable("crime_id") as UUID
        viewModel.getCrime(crimeId)
        requestContactActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode != Activity.RESULT_OK) {
                    return@registerForActivityResult
                } else {
                    result.data?.let {
                        val contactUri: Uri? = result.data!!.data
                        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                        contactUri?.let {
                            val cursor = requireActivity().contentResolver.query(
                                contactUri,
                                queryFields,
                                null,
                                null,
                                null
                            )
                            cursor?.use {
                                if (it.count == 0) {
                                    return@use
                                }
                                it.moveToFirst()
                                val suspect = it.getString(0)
                                viewModel.crime.suspect = suspect
                                viewModel.updateCrime(viewModel.crime)
                                binding.crimeSuspect.text = suspect
                            }
                        }
                    }
                }
            }
        captureActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != Activity.RESULT_OK) {
                    return@registerForActivityResult
                } else {
                    requireActivity().revokeUriPermission(
                        viewModel.photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    updatePhotoView()
                    binding.crimePhoto.announceForAccessibility("照片已拍摄完毕")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.crimeLiveData.observe(viewLifecycleOwner, { crime ->
            crime?.apply {
                viewModel.crime = this
                viewModel.photoFile = viewModel.getPhotoFile(crime)
                viewModel.photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.fanjiaxing.criminalintent.fileprovider",
                    viewModel.photoFile
                )
            }
            updateUI()
        })
    }

    @SuppressLint("QueryPermissionsNeeded", "SimpleDateFormat")
    override fun onStart() {
        super.onStart()
        binding.crimeTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.crimeSolved.setOnCheckedChangeListener { _, isChecked ->
            viewModel.crime.isSolved = isChecked
        }
        binding.crimeDate.setOnClickListener {
            DatePickerFragment.newInstance(viewModel.crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }
        binding.crimeTime.setOnClickListener {
            TimePickerFragment.newInstance(viewModel.crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }
        binding.crimeReport.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also {
                val chooserIntent = Intent.createChooser(it, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
        binding.crimeSuspect.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                requestContactActivityResultLauncher.launch(pickContactIntent)
            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }
        binding.crimeCamera.apply {
            val packageManager = requireActivity().packageManager
            val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(
                captureImageIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.photoUri)

                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(
                    captureImageIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        viewModel.photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                captureActivityResultLauncher.launch(captureImageIntent)
            }
        }

        parentFragmentManager.setFragmentResultListener(REQUEST_DATE, this,
            { requestKey, result ->
                when (requestKey) {
                    REQUEST_DATE -> {
                        val date = result.getSerializable(RESULT_DATE) as Date?
                        val time = result.getSerializable(RESULT_TIME) as Date?
                        date?.let {
                            val timeString = DateFormat.format(TIME_FORMAT, viewModel.crime.date)
                            val calendarString = DateFormat.format(DATE_FORMAT, it)
                            val ft = SimpleDateFormat("$DATE_FORMAT $TIME_FORMAT")
                            ft.parse("$calendarString $timeString")?.let { d ->
                                viewModel.crime.date = d
                            }
                            updateUI()
                        }
                        time?.let {
                            val timeString = DateFormat.format(TIME_FORMAT, it)
                            val calendarString =
                                DateFormat.format(DATE_FORMAT, viewModel.crime.date)
                            val ft = SimpleDateFormat("$DATE_FORMAT $TIME_FORMAT")
                            ft.parse("$calendarString $timeString")?.let { d ->
                                viewModel.crime.date = d
                            }
                            updateUI()
                        }
                    }
                }
            })
    }

    override fun onStop() {
        super.onStop()
        viewModel.updateCrime(viewModel.crime)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(
            viewModel.photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    private fun updateUI() {
        binding.apply {
            crimeTitle.setText(viewModel.crime.title)
            val calendar = Calendar.getInstance()
            calendar.time = viewModel.crime.date
            val date = DateFormat.format(CALENDAR_FORMAT, viewModel.crime.date)
            val time = DateFormat.format(TIME_FORMAT, viewModel.crime.date)
            crimeDate.text = date
            crimeTime.text = time
            crimeSolved.apply {
                isChecked = viewModel.crime.isSolved
                jumpDrawablesToCurrentState()
            }
            if (viewModel.crime.suspect.isNotEmpty()) {
                crimeSuspect.text = viewModel.crime.suspect
            }
        }
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (viewModel.photoFile.exists()) {
            val bitmap = getScaledBitmap(viewModel.photoFile.path, requireActivity())
            binding.crimePhoto.setImageBitmap(bitmap)
            binding.crimePhoto.contentDescription = getString(R.string.crime_photo_image_description)
        } else {
            binding.crimePhoto.setImageDrawable(null)
            binding.crimePhoto.contentDescription = getString(R.string.crime_photo_no_image_description)
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun getCrimeReport(): String {
        val solvedString = if (viewModel.crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString =
            DateFormat.format(DATE_FORMAT, viewModel.crime.date).toString()
        val suspect = if (viewModel.crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, viewModel.crime.suspect)
        }

        return getString(
            R.string.crime_report,
            viewModel.crime.title,
            dateString,
            solvedString,
            suspect
        )
    }

}