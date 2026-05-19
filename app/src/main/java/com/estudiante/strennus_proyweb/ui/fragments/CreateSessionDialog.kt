package com.estudiante.strennus_proyweb.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.data.APIService
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.data.Exercise
import com.estudiante.strennus_proyweb.databinding.DialogCreateSessionBinding
import com.estudiante.strennus_proyweb.entities.DetalleSesion
import com.estudiante.strennus_proyweb.entities.Sesion
import com.estudiante.strennus_proyweb.repository.AppRepository
import com.estudiante.strennus_proyweb.ui.adapters.EjercicioConSeries
import com.estudiante.strennus_proyweb.ui.adapters.ExerciseAdapter
import com.estudiante.strennus_proyweb.ui.adapters.SelectedExerciseAdapter
import com.estudiante.strennus_proyweb.ui.adapters.SerieTemp
import com.estudiante.strennus_proyweb.viewmodels.AppViewModelFactory
import com.estudiante.strennus_proyweb.viewmodels.SessionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreateSessionDialog : DialogFragment() {

    private var _binding: DialogCreateSessionBinding? = null
    private val binding get() = _binding!!

    private lateinit var availableAdapter: ExerciseAdapter
    private lateinit var selectedAdapter: SelectedExerciseAdapter

    // Nombre de la propiedad unificado para evitar confusiones
    private lateinit var sesionViewModel: SessionViewModel

    private val exerciseList = mutableListOf<Exercise>()
    private val allExercises = mutableListOf<Exercise>()
    private val selectedExercises = mutableListOf<EjercicioConSeries>()

    private var currentOffset = 0
    private var isLoading = false
    private var isSearchMode = true

    private val usuarioId by lazy {
        requireContext()
            .getSharedPreferences("strenuus_prefs", Context.MODE_PRIVATE)
            .getInt("usuario_id", -1)
    }

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        Retrofit.Builder()
            .baseUrl("https://wger.de/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setGravity(android.view.Gravity.BOTTOM)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        initAvailableRecyclerView()
        initSelectedRecyclerView()
        loadExercises()
        setupListeners()
    }

    private fun setupViewModel() {
        val db = AppDataBase.getInstance(requireContext())

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val localRetrofit = Retrofit.Builder()
            .baseUrl("https://wger.de/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = localRetrofit.create(APIService::class.java)

        val repository = AppRepository(
            db.usuarioDao(),
            db.sesionDao(),
            db.detalleDao(),
            db.rutinaDao(),
            apiService
        )
        val factory = AppViewModelFactory(repository)

        sesionViewModel = ViewModelProvider(this, factory)[SessionViewModel::class.java]
    }

    private fun initAvailableRecyclerView() {
        availableAdapter = ExerciseAdapter(exerciseList) { exercise ->
            addExerciseToSession(exercise)
        }
        binding.rvAvailableExercises.setHasFixedSize(true)
        binding.rvAvailableExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAvailableExercises.adapter = availableAdapter
        binding.rvAvailableExercises.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= lm.itemCount - 3 && !isLoading) {
                    loadMoreExercises()
                }
            }
        })
    }

    private fun initSelectedRecyclerView() {
        selectedAdapter = SelectedExerciseAdapter(selectedExercises) { ejercicio ->
            selectedExercises.remove(ejercicio)
            selectedAdapter.notifyDataSetChanged()
            updateSelectedSection()
        }
        binding.rvSelectedExercises.setHasFixedSize(false)
        binding.rvSelectedExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedExercises.adapter = selectedAdapter
        binding.rvSelectedExercises.isNestedScrollingEnabled = false
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSaveSession.setOnClickListener { saveSession() }

        binding.etSearchExercise.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    if (!isSearchMode) showSearchMode()
                    exerciseList.clear()
                    if (query.isEmpty()) {
                        exerciseList.addAll(allExercises)
                    } else {
                        exerciseList.addAll(
                            allExercises.filter {
                                it.name.lowercase().contains(query.lowercase())
                            }
                        )
                    }
                    availableAdapter.notifyDataSetChanged()
                }
            }
        })

        binding.etSearchExercise.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !isSearchMode) showSearchMode()
        }
    }

    private fun showSearchMode() {
        isSearchMode = true
        binding.rvAvailableExercises.visibility = View.VISIBLE
        if (selectedExercises.isNotEmpty()) {
            binding.selectedExercisesSection.visibility = View.VISIBLE
        }
    }

    private fun updateSelectedSection() {
        if (selectedExercises.isEmpty()) {
            binding.selectedExercisesSection.visibility = View.GONE
            showSearchMode()
        } else {
            binding.selectedExercisesSection.visibility = View.VISIBLE
            selectedAdapter.notifyDataSetChanged()
        }
    }

    private fun addExerciseToSession(exercise: Exercise) {
        val existing = selectedExercises.find { it.nombre == exercise.name }
        if (existing != null) {
            existing.series.add(SerieTemp())
            selectedAdapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Serie agregada a ${exercise.name}", Toast.LENGTH_SHORT).show()
        } else {
            selectedExercises.add(
                EjercicioConSeries(nombre = exercise.name, series = mutableListOf(SerieTemp()))
            )
            selectedAdapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "${exercise.name} agregado", Toast.LENGTH_SHORT).show()
        }
        binding.etSearchExercise.text?.clear()
        binding.selectedExercisesSection.visibility = View.VISIBLE
    }

    private fun saveSession() {
        val nombre = binding.etSessionName.text.toString().trim()
        if (nombre.isEmpty()) {
            binding.etSessionName.error = "Escribe un nombre para la sesión"
            return
        }
        if (selectedExercises.isEmpty()) {
            Toast.makeText(requireContext(), "Agrega al menos un ejercicio", Toast.LENGTH_SHORT).show()
            return
        }
        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Error: no hay usuario logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val ahora = System.currentTimeMillis()
        val sesion = Sesion(
            usuarioId = usuarioId,
            nombre = nombre,
            fecha = ahora,
            horaInicio = ahora
        )

        sesionViewModel.crearSesion(sesion) { sesionId ->
            selectedExercises.forEach { ejercicio ->
                ejercicio.series.forEachIndexed { index, serie ->
                    val detalle = DetalleSesion(
                        sesionId = sesionId.toInt(),
                        nombreEjercicio = ejercicio.nombre,
                        series = index + 1,
                        repeticiones = serie.reps,
                        peso = serie.peso,
                        notas = null
                    )
                    sesionViewModel.agregarEjercicio(detalle)
                }
            }
            // requireActivity() se asegura de ejecutarse en el Main Thread de forma nativa e integrada en Fragments
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "Sesión \"$nombre\" guardada",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }
    }

    private fun loadExercises() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = retrofit.create(APIService::class.java).getExercises()
                withContext(Dispatchers.Main) {
                    if (call.isSuccessful) {
                        val exercises = call.body()?.exercises ?: emptyList()
                        exerciseList.clear()
                        exerciseList.addAll(exercises)
                        allExercises.clear()
                        allExercises.addAll(exercises)
                        availableAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateSessionDialog", "Error: ${e.message}")
            }
        }
    }

    private fun loadMoreExercises() {
        if (isLoading) return
        isLoading = true
        currentOffset += 100
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = retrofit.create(APIService::class.java).getExercises(offset = currentOffset)
                withContext(Dispatchers.Main) {
                    if (call.isSuccessful) {
                        val exercises = call.body()?.exercises ?: emptyList()
                        exerciseList.addAll(exercises)
                        allExercises.addAll(exercises)
                        availableAdapter.notifyDataSetChanged()
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}