package com.estudiante.strennus_proyweb.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.estudiante.strennus_proyweb.R
import com.estudiante.strennus_proyweb.databinding.ActivityMainMenuBinding
import com.estudiante.strennus_proyweb.ui.fragments.HomeFragment
import com.estudiante.strennus_proyweb.ui.fragments.ProfileFragment
import com.estudiante.strennus_proyweb.ui.fragments.RankingFragment
import com.estudiante.strennus_proyweb.ui.fragments.SessionsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

loadFragment(HomeFragment())

binding.bottomNavigation.setOnItemSelectedListener { item ->
    when (item.itemId) {
        R.id.navigation_home -> loadFragment(HomeFragment())
        R.id.navigation_sessions -> loadFragment(SessionsFragment())
        R.id.navigation_ranking -> loadFragment(RankingFragment())
        R.id.navigation_profile -> loadFragment(ProfileFragment())
    }
    true
}

binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.viewPager, fragment)
            .commit()
    }
}