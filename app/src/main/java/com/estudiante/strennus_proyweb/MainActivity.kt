package com.estudiante.strennus_proyweb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.estudiante.strennus_proyweb.databinding.ActivityMainMenuBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}