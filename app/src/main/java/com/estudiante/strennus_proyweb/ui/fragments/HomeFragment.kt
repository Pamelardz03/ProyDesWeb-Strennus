package com.estudiante.strennus_proyweb.ui.fragments

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.FragmentHomeBinding
import com.estudiante.strennus_proyweb.repository.AppRepository
import com.estudiante.strennus_proyweb.ui.adapters.SesionAdapter
import com.estudiante.strennus_proyweb.viewmodels.AppViewModelFactory
import com.estudiante.strennus_proyweb.viewmodels.HomeViewModel

class HomeFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var sesionAdapter: SesionAdapter

    // Sensor de pasos
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepCount = 0
    private val stepGoal = 10000

    private val usuarioId by lazy {
        requireContext().getSharedPreferences("strenuus_prefs", Context.MODE_PRIVATE)
            .getInt("usuario_id", 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupRecyclerView()
    setupViewModel()
    observeViewModel()
    setupStepSensor()

    parentFragmentManager.setFragmentResultListener(
        "session_created", viewLifecycleOwner
    ) { _, _ ->
        viewModel.cargarSesiones(usuarioId)
    }

    if (usuarioId != -1) {
        viewModel.cargarUsuario(usuarioId)
        viewModel.cargarSesiones(usuarioId)
    }

    binding.tvViewMoreSessions.setOnClickListener {
        parentFragmentManager.beginTransaction()
            .replace(com.estudiante.strennus_proyweb.R.id.viewPager, SessionsFragment())
            .addToBackStack(null)
            .commit()
    }
}

    override fun onResume() {
        super.onResume()
        viewModel.cargarSesiones(usuarioId)
        // Registrar sensor al volver al fragment
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Desregistrar sensor al salir del fragment para ahorrar batería
        sensorManager.unregisterListener(this)
    }

    // ─── Sensor de pasos ─────────────────────────────────────────────────────
    private fun setupStepSensor() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            // El dispositivo no tiene sensor de pasos
            binding.tvStepCount.text = "N/D"
            binding.tvStepCountActivity.text = "N/D"
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            // TYPE_STEP_COUNTER acumula desde el arranque del dispositivo
            // Guardamos el valor inicial para calcular pasos de hoy
            val prefs = requireContext().getSharedPreferences("strenuus_prefs", Context.MODE_PRIVATE)
            val initialSteps = prefs.getInt("initial_steps", -1)

            if (initialSteps == -1) {
                // Primera vez — guardar valor inicial
                prefs.edit().putInt("initial_steps", event.values[0].toInt()).apply()
                stepCount = 0
            } else {
                stepCount = event.values[0].toInt() - initialSteps
            }

            updateStepUI()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateStepUI() {
        val progress = ((stepCount.toFloat() / stepGoal) * 100).toInt().coerceAtMost(100)

        // Card de Pasos
        binding.tvStepCount.text = "%,d".format(stepCount)
        binding.tvStepSummary.text = "%,d/%,d".format(stepCount, stepGoal)
        binding.progressSteps.progress = progress

        // Card de Actividad Diaria
        binding.tvStepCountActivity.text = "%,d".format(stepCount)
        binding.tvProgressPercent.text = "$progress%"
        binding.circularProgress.progress = progress

        // Calorías aproximadas (0.04 kcal por paso)
        val calories = (stepCount * 0.04).toInt()
        binding.tvCalories.text = "$calories"

        // Distancia aproximada (0.762 metros por paso promedio)
        val distanceKm = (stepCount * 0.000762)
        binding.tvDistance.text = "%.2f".format(distanceKm)
    }

    // ─── ViewModel ───────────────────────────────────────────────────────────
    private fun setupViewModel() {
        val db = AppDataBase.getInstance(requireContext())

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://wger.de/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(com.estudiante.strennus_proyweb.data.APIService::class.java)

        val repository = AppRepository(
            db.usuarioDao(),
            db.sesionDao(),
            db.detalleDao(),
            db.rutinaDao(),
            apiService
        )
        val factory = AppViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    private fun setupRecyclerView() {
        sesionAdapter = SesionAdapter(
            onItemClick = { sesion ->
                val fragment = SessionDetailFragment.newInstance(sesion.id)
                parentFragmentManager.beginTransaction()
                    .replace(com.estudiante.strennus_proyweb.R.id.viewPager, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { sesion ->
                viewModel.eliminarSesion(sesion)
            }
        )
        binding.rvSessionsPreview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sesionAdapter
            setHasFixedSize(false)
        }
    }

    private fun observeViewModel() {
        viewModel.sesiones.observe(viewLifecycleOwner) { listaSesiones ->
            sesionAdapter.submitList(listaSesiones?.take(3) ?: emptyList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}