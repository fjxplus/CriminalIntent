package com.fanjiaxing.criminalintent.logic

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.fanjiaxing.criminalintent.logic.dao.CrimeDatabase
import com.fanjiaxing.criminalintent.logic.model.Crime
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

class Repository private constructor(context: Context) {

    companion object {
        private var INSTANCE: Repository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = Repository(context)
            }
        }

        fun get(): Repository {
            return INSTANCE ?: throw IllegalStateException("Repository must be initialize")
        }
    }

    private val crimeDao = CrimeDatabase.getDatabase(context).crimeDao()

    private val executor = Executors.newSingleThreadExecutor()

    private val filesDir = context.applicationContext.filesDir

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun addCrime(crime: Crime) {
        executor.execute{
            crimeDao.addCrime(crime)
        }
    }

    fun updateCrime(crime: Crime) {
        executor.execute{
            crimeDao.updateCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)
}