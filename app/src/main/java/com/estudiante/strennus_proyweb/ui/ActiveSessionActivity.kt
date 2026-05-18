package com.estudiante.strennus_proyweb.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.ActivityActiveSessionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActiveSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActiveSessionBinding
    //ejemplo
    private var ejercicios = listOf<String>()
    private var ejercicioActual = 0
    private var serieActual = 1
    private var totalSeries = 3
    private var totalReps = 15
    private var tiempoDescanso = 60
    private var enDescanso = false
    private var timer: CountDownTimer? = null
    private val sesionId by lazy { intent.getIntExtra("sesion_id", -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarEjercicios()

        binding.btnPause.setOnClickListener {
            timer?.cancel()
        }

        binding.btnSkip.setOnClickListener {
            timer?.cancel()
            siguienteEtapa()
        }

        binding.btnBackControl.setOnClickListener {
            timer?.cancel()
            finish()
        }

        binding.btnBack.setOnClickListener {
            timer?.cancel()
            finish()
        }
    }

    private fun cargarEjercicios() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDataBase.getInstance(this@ActiveSessionActivity)
            val detalles = db.detalleDao().getDetallesBySesion(sesionId)
            val nombres = detalles.map { it.nombreEjercicio }.distinct()

            withContext(Dispatchers.Main) {
                ejercicios = nombres
                if (ejercicios.isNotEmpty()) {
                    mostrarEjercicioActual()
                    iniciarTimer(totalReps * 3)
                }
            }
        }
    }

    private fun mostrarEjercicioActual() {
        if (ejercicioActual >= ejercicios.size) {
            irAResumen()
            return
        }
        binding.tvExerciseName.text = ejercicios[ejercicioActual]
        binding.tvSetInfo.text = "Serie $serieActual de $totalSeries"
        binding.tvStatus.text = "$totalReps repeticiones"
        binding.tvProgress.text = "${ejercicioActual + 1}/${ejercicios.size}"
        binding.progressBar.progress = ((ejercicioActual + 1) * 100 / ejercicios.size)
        enDescanso = false
    }

    private fun iniciarTimer(segundos: Int) {
        timer?.cancel()
        binding.tvTimer.text = segundos.toString()

        timer = object : CountDownTimer(segundos * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                siguienteEtapa()
            }
        }.start()
    }

    private fun siguienteEtapa() {
        if (!enDescanso) {
            if (serieActual < totalSeries) {
                enDescanso = true
                binding.tvSetInfo.text = "Serie ${serieActual + 1} de $totalSeries"
                binding.tvStatus.text = "Descanso"
                iniciarTimer(tiempoDescanso)
            } else {
                serieActual = 1
                ejercicioActual++
                mostrarEjercicioActual()
                if (ejercicioActual < ejercicios.size) {
                    iniciarTimer(totalReps * 3)
                }
            }
        } else {
            enDescanso = false
            serieActual++
            mostrarEjercicioActual()
            iniciarTimer(totalReps * 3)
        }
    }

    private fun irAResumen() {
        timer?.cancel()
        val intent = Intent(this, SessionSummaryActivity::class.java)
        intent.putExtra("sesion_id", sesionId)
        intent.putExtra("ejercicios_completados", ejercicios.size)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}