package com.estudiante.strennus_proyweb.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.data.APIService
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.data.Exercise
import com.estudiante.strennus_proyweb.data.Translation
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
    private val ejerciciosUsuario = mutableListOf<Exercise>()
    private val ejerciciosAPI = mutableListOf<Exercise>()
    private val selectedExercises = mutableListOf<EjercicioConSeries>()

    private var currentOffset = 0
    private var isLoading = false
    private var isSearchMode = true
    private var mostrarSoloMios = false
    private var selectedImageUri: Uri? = null

    private val usuarioId by lazy {
        requireContext()
            .getSharedPreferences("strenuus_prefs", Context.MODE_PRIVATE)
            .getInt("usuario_id", -1)
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivSessionImage.setImageURI(it)
            binding.cardImagePreview.visibility = View.VISIBLE
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            binding.ivSessionImage.setImageBitmap(it)
            binding.cardImagePreview.visibility = View.VISIBLE
            val path = android.provider.MediaStore.Images.Media.insertImage(
                requireContext().contentResolver, it, "Sesión", null
            )
            selectedImageUri = Uri.parse(path)
        }
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
            db.ejercicioPersonalizadoDao(),
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
                    if (!mostrarSoloMios) loadMoreExercises()
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

        binding.btnCreateExercise.setOnClickListener {
            val dialog = CreateExerciseDialog { ejercicio ->
                val nuevoExercise = Exercise(
                    id = -1,
                    translations = listOf(Translation(language = 2, name = ejercicio.nombre, description = ""))
                )
                ejerciciosUsuario.add(0, nuevoExercise)
                allExercises.add(0, nuevoExercise)
                exerciseList.add(0, nuevoExercise)
                availableAdapter.notifyDataSetChanged()

                selectedExercises.add(ejercicio)
                selectedAdapter.notifyDataSetChanged()
                binding.selectedExercisesSection.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "${ejercicio.nombre} creado y agregado", Toast.LENGTH_SHORT).show()
            }
            dialog.show(parentFragmentManager, "CreateExerciseDialog")
        }

        binding.btnSaveSession.setOnClickListener { saveSession() }
        binding.btnAddImage.setOnClickListener { showImageOptions() }
        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            binding.ivSessionImage.setImageDrawable(null)
            binding.cardImagePreview.visibility = View.GONE
        }

        // Filtro Todos
        binding.btnFiltroTodos.setOnClickListener {
            mostrarSoloMios = false
            setFiltroActivo(isTodos = true)
            val query = binding.etSearchExercise.text.toString().trim()
            exerciseList.clear()
            if (query.isEmpty()) {
                exerciseList.addAll(allExercises)
            } else {
                exerciseList.addAll(allExercises.filter {
                    it.name.lowercase().contains(query.lowercase())
                })
            }
            availableAdapter.notifyDataSetChanged()
        }

        // Filtro Mis ejercicios
        binding.btnFiltroMios.setOnClickListener {
            mostrarSoloMios = true
            setFiltroActivo(isTodos = false)
            val query = binding.etSearchExercise.text.toString().trim()
            exerciseList.clear()
            if (query.isEmpty()) {
                exerciseList.addAll(ejerciciosUsuario)
            } else {
                exerciseList.addAll(ejerciciosUsuario.filter {
                    it.name.lowercase().contains(query.lowercase())
                })
            }
            availableAdapter.notifyDataSetChanged()
        }

        binding.etSearchExercise.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    if (!isSearchMode) showSearchMode()
                    val source = if (mostrarSoloMios) ejerciciosUsuario else allExercises
                    exerciseList.clear()
                    if (query.isEmpty()) {
                        exerciseList.addAll(source)
                    } else {
                        exerciseList.addAll(source.filter {
                            it.name.lowercase().contains(query.lowercase())
                        })
                    }
                    availableAdapter.notifyDataSetChanged()
                }
            }
        })

        binding.etSearchExercise.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !isSearchMode) showSearchMode()
        }
    }

    // Cambia el color del botón activo
    private fun setFiltroActivo(isTodos: Boolean) {
        val rojo = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E60000"))
        val gris = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1A1A1A"))
        binding.btnFiltroTodos.backgroundTintList = if (isTodos) rojo else gris
        binding.btnFiltroMios.backgroundTintList = if (isTodos) gris else rojo
    }

    private fun showImageOptions() {
        val options = arrayOf("Tomar foto", "Elegir de galería")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Agregar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(null)
                    1 -> galleryLauncher.launch("image/*")
                }
            }.show()
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
            horaInicio = ahora,
            imagenPath = selectedImageUri?.toString(),
            notas = binding.etSessionNotes.text.toString().trim().ifEmpty { null }
        )

        sesionViewModel.crearSesion(sesion) { sesionId ->
            selectedExercises.forEach { ejercicio ->
                val totalSeries = ejercicio.series.size
                val primerasSerie = ejercicio.series.firstOrNull()
                val detalle = DetalleSesion(
                    sesionId = sesionId.toInt(),
                    nombreEjercicio = ejercicio.nombre,
                    series = totalSeries,
                    repeticiones = primerasSerie?.reps ?: 12,
                    peso = primerasSerie?.peso
                )
                sesionViewModel.agregarEjercicio(detalle)
            }
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Sesión \"$nombre\" guardada", Toast.LENGTH_SHORT).show()
                parentFragmentManager.setFragmentResult("session_created", Bundle())
                dismiss()
            }
        }
    }

    private fun loadExercises() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Cargar ejercicios del usuario
                val db = AppDataBase.getInstance(requireContext())
                val repository = AppRepository(
                    db.usuarioDao(), db.sesionDao(), db.detalleDao(),
                    db.rutinaDao(), db.ejercicioPersonalizadoDao()
                )
                val personalizados = repository.obtenerEjerciciosPersonalizados(usuarioId)
                val listaUsuario = personalizados.map { ep ->
                    Exercise(
                        id = -ep.id,
                        translations = listOf(Translation(
                            language = 2,
                            name = if (!ep.categoria.isNullOrEmpty()) "${ep.nombre} (${ep.categoria})" else ep.nombre,
                            description = ep.descripcion ?: ""
                        ))
                    )
                }

                // 2. Cargar ejercicios de la API
                val call = retrofit.create(APIService::class.java).getExercises()

                withContext(Dispatchers.Main) {
                    ejerciciosUsuario.clear()
                    ejerciciosUsuario.addAll(listaUsuario)

                    ejerciciosAPI.clear()
                    if (call.isSuccessful) {
                        ejerciciosAPI.addAll(call.body()?.exercises ?: emptyList())
                    }

                    allExercises.clear()
                    allExercises.addAll(ejerciciosUsuario)
                    allExercises.addAll(ejerciciosAPI)

                    exerciseList.clear()
                    exerciseList.addAll(allExercises)
                    availableAdapter.notifyDataSetChanged()
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
                        ejerciciosAPI.addAll(exercises)
                        allExercises.addAll(exercises)
                        if (!mostrarSoloMios) {
                            exerciseList.addAll(exercises)
                            availableAdapter.notifyDataSetChanged()
                        }
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