package com.fanjiaxing.criminalintent.logic.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fanjiaxing.criminalintent.logic.model.Crime
import com.fanjiaxing.criminalintent.logic.model.CrimeTypeConverters

@Database(entities = [Crime::class], version = 2, exportSchema = false)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDao(): CrimeDao

    companion object {
        private var instance: CrimeDatabase? = null

        private val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
            }

        }

        fun getDatabase(context: Context): CrimeDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                CrimeDatabase::class.java,
                "crime-database"
            ).addMigrations(migration_1_2).build().apply {
                instance = this
            }
        }
    }
}