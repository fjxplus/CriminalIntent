package com.fanjiaxing.criminalintent

import android.app.Application
import com.fanjiaxing.criminalintent.logic.Repository

class CriminalIntentApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
    }
}