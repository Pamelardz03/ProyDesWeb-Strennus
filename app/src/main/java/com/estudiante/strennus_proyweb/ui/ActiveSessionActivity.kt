package com.estudiante.strennus_proyweb.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.estudiante.strennus_proyweb.R
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.ActivityActiveSessionBinding
import com.estudiante.strennus_proyweb.entities.DetalleSesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActiveSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActiveSessionBinding

    private var detalles = listOf<DetalleSesion>()
    private var ejercicioActual = 0
    private var serieActual = 1
    private var enDescanso = false
    private var descansoEntreEjercicios = false
    private var enPausa = false

    private var timer: CountDownTimer? = null
    private var millisRestantes = 0L
    private var totalMillisEtapa = 0L
    private var sesionStartMs = 0L

    private val tiempoDescanso = 60

    private var totalEtapas = 1
    private var etapaActual = 0

    private val sesionId by lazy { intent.getIntExtra("sesion_id", -1) }

    private val detalleActual get() = detalles[ejercicioActual]
    private val totalSeries get() = detalleActual.series
    private val totalReps get() = detalleActual.repeticiones

    private data class Etapa(
        val ejercicioIdx: Int,
        val serie: Int,
        val esDescanso: Boolean,
        val descansoEntreEjs: Boolean,
        val millis: Long
    )
    private val historial = mutableListOf<Etapa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesionStartMs = System.currentTimeMillis()
        cargarEjercicios()

        binding.btnPause.setOnClickListener {
            if (enPausa) {
                enPausa = false
                binding.btnPause.setIconResource(R.drawable.ic_pause)
                iniciarTimerDesde(millisRestantes)
            } else {
                enPausa = true
                timer?.cancel()
                binding.btnPause.setIconResource(R.drawable.ic_play)
            }
        }

        binding.btnSkip.setOnClickListener { timer?.cancel(); siguienteEtapa() }

        binding.btnBack.setOnClickListener {
            timer?.cancel()
            android.app.AlertDialog.Builder(this)
                .setTitle("¿Salir de la sesión?")
                .setMessage("Se perderá el progreso actual")
                .setPositiveButton("Salir") { _, _ -> finish() }
                .setNegativeButton("Continuar") { _, _ -> iniciarTimerDesde(millisRestantes) }
                .show()
        }

        binding.btnBackControl.setOnClickListener {
            timer?.cancel()
            if (historial.size > 1) {
                historial.removeAt(historial.lastIndex)
                val anterior = historial[historial.lastIndex]
                historial.removeAt(historial.lastIndex)

                ejercicioActual = anterior.ejercicioIdx
                serieActual = anterior.serie
                enDescanso = anterior.esDescanso
                descansoEntreEjercicios = anterior.descansoEntreEjs

                etapaActual = (etapaActual - 1).coerceAtLeast(0)
                binding.progressBar.progress = (etapaActual * 100 / totalEtapas).coerceAtMost(100)
                binding.tvProgress.text = "${ejercicioActual + 1}/${detalles.size}"

                if (enDescanso) {
                    binding.tvExerciseName.text = if (descansoEntreEjercicios)
                        "Siguiente: ${detalles[ejercicioActual].nombreEjercicio}"
                    else detalleActual.nombreEjercicio
                    binding.tvSetInfo.text = "Serie $serieActual de $totalSeries"
                    binding.tvStatus.text = if (descansoEntreEjercicios) "Descanso entre ejercicios" else "Descanso entre series"
                    binding.tvRepsRemaining.text = ""
                    binding.statusBadge.setCardBackgroundColor(getColor(R.color.yellow_intensity))
                } else {
                    binding.tvExerciseName.text = detalleActual.nombreEjercicio
                    binding.tvSetInfo.text = "Serie $serieActual de $totalSeries"
                    binding.tvStatus.text = "$totalReps repeticiones"
                    binding.statusBadge.setCardBackgroundColor(getColor(R.color.red_primary))
                }

                iniciarTimerDesde(anterior.millis)
            }
        }
    }

    private fun cargarEjercicios() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDataBase.getInstance(this@ActiveSessionActivity)
            val lista = db.detalleDao().getDetallesBySesion(sesionId)
            withContext(Dispatchers.Main) {
                detalles = lista
                totalEtapas = detalles.mapIndexed { index, detalle ->
                    val esUltimo = index == detalles.size - 1
                    detalle.series + (detalle.series - 1) + if (esUltimo) 0 else 1
                }.sum().coerceAtLeast(1)
                etapaActual = 0

                if (detalles.isNotEmpty()) {
                    ejercicioActual = 0
                    serieActual = 1
                    actualizarUI()
                    iniciarTimerDesde(totalReps * 3 * 1000L)
                }
            }
        }
    }

    private fun avanzarEtapa() {
        etapaActual++
        binding.progressBar.progress = (etapaActual * 100 / totalEtapas).coerceAtMost(100)
        binding.tvProgress.text = "${ejercicioActual + 1}/${detalles.size}"
    }

    private fun actualizarUI() {
        binding.tvProgress.text = "${ejercicioActual + 1}/${detalles.size}"
        binding.tvExerciseName.text = detalleActual.nombreEjercicio
        binding.tvSetInfo.text = "Serie $serieActual de $totalSeries"
        binding.tvStatus.text = "$totalReps repeticiones"
        binding.tvRepsRemaining.text = ""
        binding.statusBadge.setCardBackgroundColor(getColor(R.color.red_primary))
        enDescanso = false
        descansoEntreEjercicios = false
    }

    private fun iniciarTimerDesde(millis: Long) {
        historial.add(Etapa(ejercicioActual, serieActual, enDescanso, descansoEntreEjercicios, millis))
        timer?.cancel()
        totalMillisEtapa = millis
        millisRestantes = millis
        binding.circularProgress.max = 100
        binding.circularProgress.progress = 100

        timer = object : CountDownTimer(millis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                millisRestantes = millisUntilFinished
                binding.tvTimer.text = (millisUntilFinished / 1000).toString()
                binding.circularProgress.progress =
                    ((millisUntilFinished.toFloat() / totalMillisEtapa) * 100).toInt()
                if (!enDescanso) {
                    val reps = kotlin.math.ceil((millisUntilFinished / 1000) / 3.0).toInt()
                    binding.tvRepsRemaining.text = "~$reps reps restantes"
                }
            }
            override fun onFinish() {
                binding.circularProgress.progress = 0
                siguienteEtapa()
            }
        }.start()
    }

    private fun siguienteEtapa() {
        when {
            descansoEntreEjercicios -> {
                actualizarUI()
                iniciarTimerDesde(totalReps * 3 * 1000L)
            }
            enDescanso -> {
                avanzarEtapa()
                enDescanso = false
                binding.statusBadge.setCardBackgroundColor(getColor(R.color.red_primary))
                binding.tvSetInfo.text = "Serie $serieActual de $totalSeries"
                binding.tvStatus.text = "$totalReps repeticiones"
                binding.tvRepsRemaining.text = ""
                iniciarTimerDesde(totalReps * 3 * 1000L)
            }
            serieActual < totalSeries -> {
                avanzarEtapa()
                enDescanso = true
                serieActual++
                binding.tvSetInfo.text = "Serie $serieActual de $totalSeries"
                binding.tvStatus.text = "Descanso"
                binding.tvRepsRemaining.text = ""
                binding.statusBadge.setCardBackgroundColor(getColor(R.color.yellow_intensity))
                iniciarTimerDesde(tiempoDescanso * 1000L)
            }
            else -> {
                avanzarEtapa()
                ejercicioActual++
                serieActual = 1
                if (ejercicioActual >= detalles.size) {
                    irAResumen()
                } else {
                    enDescanso = true
                    descansoEntreEjercicios = true
                    binding.tvExerciseName.text = "Siguiente: ${detalles[ejercicioActual].nombreEjercicio}"
                    binding.tvSetInfo.text = ""
                    binding.tvStatus.text = "Descanso entre ejercicios"
                    binding.tvRepsRemaining.text = ""
                    binding.statusBadge.setCardBackgroundColor(getColor(R.color.yellow_intensity))
                    iniciarTimerDesde(tiempoDescanso * 1000L)
                }
            }
        }
    }

    private fun irAResumen() {
        timer?.cancel()
        val duracionMin = ((System.currentTimeMillis() - sesionStartMs) / 1000 / 60).toInt()
        startActivity(Intent(this, SessionSummaryActivity::class.java).apply {
            putExtra("sesion_id", sesionId)
            putExtra("ejercicios_completados", detalles.size)
            putExtra("duracion_minutos", duracionMin)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}