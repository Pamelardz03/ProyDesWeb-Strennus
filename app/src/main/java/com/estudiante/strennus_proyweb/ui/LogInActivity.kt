package com.estudiante.strennus_proyweb.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.estudiante.strennus_proyweb.databinding.ActivityLoginBinding

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}