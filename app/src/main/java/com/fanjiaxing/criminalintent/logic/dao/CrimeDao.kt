package com.fanjiaxing.criminalintent.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fanjiaxing.criminalintent.logic.model.Crime
import java.util.*

@Dao
interface CrimeDao {

    @Query("select * from Crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("select * from Crime where id = (:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Insert
    fun addCrime(crime: Crime)

    @Update
    fun updateCrime(crime: Crime)
}