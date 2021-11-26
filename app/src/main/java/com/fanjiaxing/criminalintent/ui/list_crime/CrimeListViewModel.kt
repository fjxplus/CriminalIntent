package com.fanjiaxing.criminalintent.ui.list_crime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.fanjiaxing.criminalintent.logic.Repository
import com.fanjiaxing.criminalintent.logic.model.Crime

class CrimeListViewModel: ViewModel() {

    private val crimeList = MutableLiveData<Any?>()

    val crimeLiveData = Transformations.switchMap(crimeList){
        Repository.get().getCrimes()
    }

    fun getCrimes(){
        crimeList.value = crimeList.value
    }
    fun addCrime(crime: Crime){
        Repository.get().addCrime(crime)
    }
}