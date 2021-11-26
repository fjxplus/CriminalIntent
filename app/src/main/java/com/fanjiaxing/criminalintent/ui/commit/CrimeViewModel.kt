package com.fanjiaxing.criminalintent.ui.commit

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.fanjiaxing.criminalintent.logic.Repository
import com.fanjiaxing.criminalintent.logic.model.Crime
import java.io.File
import java.util.*

class CrimeViewModel: ViewModel() {

    private val repository = Repository.get()

    var crime = Crime()

    lateinit var photoFile: File

    lateinit var photoUri: Uri

    private val crimeId = MutableLiveData<UUID>()
    val crimeLiveData = Transformations.switchMap(crimeId){crimeId ->
        repository.getCrime(crimeId)
    }

    fun getCrime(crimeId: UUID){
        this.crimeId.value = crimeId
    }

    fun addCrime(crime: Crime) = repository.addCrime(crime)

    fun updateCrime(crime: Crime) = repository.updateCrime(crime)

    fun getPhotoFile(crime: Crime) = repository.getPhotoFile(crime)
}