package com.estudiante.strennus_proyweb.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.strennus_proyweb.data.APIService
import com.estudiante.strennus_proyweb.data.Exercise
import com.estudiante.strennus_proyweb.ui.adapters.ExerciseAdapter
import com.estudiante.strennus_proyweb.databinding.FragmentExercisesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class ExercisesFragment : Fragment(),
    SearchView.OnQueryTextListener {

    // --- 9.1 Propiedades ---
    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExerciseAdapter
    private val exerciseList = mutableListOf<Exercise>()


    // --- 9.2 Configuración de Retrofit (by lazy) ---
    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://wger.de/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- 9.3 onCreateView ---
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    // --- 9.3 onViewCreated (equivalente a onCreate) ---
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchExercise.setOnQueryTextListener(this)

        initRecyclerView()
        loadExercises()
    }



    // --- 9.4 Inicializar RecyclerView ---
    private fun initRecyclerView() {
        adapter = ExerciseAdapter(exerciseList)
        binding.rvExercises.setHasFixedSize(true)
        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = adapter
    }

    // --- 9.5 Eventos del SearchView ---
    override fun onQueryTextChange(newText: String?): Boolean {
        return true // No hacemos nada al escribir letra por letra
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (query.isNotEmpty()) {
            searchByName(query.lowercase(Locale.ROOT))
        }
        return true
    }

    // --- Carga inicial ---
    private fun loadExercises() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvExercises.visibility = View.GONE
        binding.tvError.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = retrofit.create(APIService::class.java)
                    .getExercises()

                Log.d("ExercisesFragment", "Response: ${call.body()}")

                withContext(Dispatchers.Main) {
                    if (call.isSuccessful) {
                        val exercises = call.body()?.exercises ?: emptyList<Exercise>()
                        exerciseList.clear()
                        exerciseList.addAll(exercises)
                        adapter.notifyDataSetChanged()
                        binding.progressBar.visibility = View.GONE
                        binding.rvExercises.visibility = View.VISIBLE
                    } else {
                        showError("Error al cargar ejercicios")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    // --- 9.6 searchByName ---
    private fun searchByName(query: String) {
        // 1. Mostrar spinner, ocultar todo lo demás
        binding.progressBar.visibility = View.VISIBLE
        binding.rvExercises.visibility = View.GONE
        binding.tvError.visibility = View.GONE

        // 2. Lanzar coroutine en hilo de IO
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 3. Hacer la petición HTTP
                val call = retrofit.create(APIService::class.java)
                    .searchExercises(name = query)

                Log.d("ExercisesFragment", "Search: ${call.body()}")

                // 4. Volver al hilo principal para actualizar UI
                withContext(Dispatchers.Main) {
                    if (call.isSuccessful) {
                        val exercises = call.body()?.exercises ?: emptyList<Exercise>()
                        exerciseList.clear()
                        exerciseList.addAll(exercises as List<Exercise>)
                        adapter.notifyDataSetChanged()
                        binding.progressBar.visibility = View.GONE
                        binding.rvExercises.visibility = View.VISIBLE

                        if (exercises.isEmpty()) {
                            showError("No se encontraron ejercicios para \"$query\"")
                        }
                    } else {
                        showError("Ejercicio no encontrado")
                    }
                    hideKeyboard()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error de conexión: ${e.message}")
                    hideKeyboard()
                }
            }
        }
    }

    // --- 9.7 Funciones auxiliares ---
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvExercises.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message
    }

    private fun hideKeyboard() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        imm.hideSoftInputFromWindow(binding.viewRoot.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ExercisesFragment()
    }
}