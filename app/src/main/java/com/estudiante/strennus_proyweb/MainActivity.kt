package com.estudiante.strennus_proyweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.estudiante.strennus_proyweb.databinding.ActivityMainMenuBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar HomeFragment al inicio
        loadFragment(HomeFragment.newInstance())

        // Escuchar clicks del BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment.newInstance())
                    true
                }
                R.id.navigation_sessions -> {
                    true
                }
                R.id.navigation_ranking -> {
                    true
                }
                R.id.navigation_profile -> {
                    true
                }
                else -> false
            }
        }

        // Seleccionar Inicio por defecto
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.viewPager, fragment)
            .commit()
    }
}