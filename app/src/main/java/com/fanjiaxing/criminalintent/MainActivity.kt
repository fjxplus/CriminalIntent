package com.fanjiaxing.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fanjiaxing.criminalintent.databinding.ActivityMainBinding
import com.fanjiaxing.criminalintent.ui.commit.CrimeFragment
import com.fanjiaxing.criminalintent.ui.list_crime.CrimeListFragment
import java.util.*

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //判断容器试图中是否已存在fragment（当旋转屏幕Activity重建时会重建fragment队列）
        val currentFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)
        if (currentFragment == null) {
            val crimeListFragment = CrimeListFragment()
            supportFragmentManager.beginTransaction()//开启fragment事务来添加、移除、附加、分离和替换fragment队列中的fragment
                .add(binding.fragmentContainer.id, crimeListFragment).commit()
        }
    }

    override fun onCrimeSelected(crimeId: UUID) {
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment)
            .addToBackStack(null).commit()
    }
}