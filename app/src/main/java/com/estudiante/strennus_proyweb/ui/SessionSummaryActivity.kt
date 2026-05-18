package com.estudiante.strennus_proyweb.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.estudiante.strennus_proyweb.databinding.ActivitySessionSummaryBinding

class SessionSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionSummaryBinding

    private val sesionId by lazy { intent.getIntExtra("sesion_id", -1) }
    private val ejerciciosCompletados by lazy { intent.getIntExtra("ejercicios_completados", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val puntos = ejerciciosCompletados * 250

        binding.tvExercisesCompleted.text = ejerciciosCompletados.toString()
        binding.tvPointsGained.text = puntos.toString()
        binding.tvDuration.text = "En progreso"

        binding.btnClose.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                "¡Completé mi sesión en Strenuus! Ejercicios: $ejerciciosCompletados, Puntos: $puntos")
            startActivity(Intent.createChooser(shareIntent, "Compartir en"))
        }
    }
}