package com.estudiante.strennus_proyweb.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.data.Exercise
import com.estudiante.strennus_proyweb.ui.adapters.ExerciseAdapter
import com.estudiante.strennus_proyweb.databinding.FragmentExercisesBinding
import com.estudiante.strennus_proyweb.repository.AppRepository
import com.estudiante.strennus_proyweb.viewmodels.AppViewModelFactory
import com.estudiante.strennus_proyweb.viewmodels.ExercisesViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class ExercisesFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExercisesViewModel
    private lateinit var adapter: ExerciseAdapter
    private val exerciseList = mutableListOf<Exercise>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchExercise.setOnQueryTextListener(this)

        setupViewModel()
        initRecyclerView()
        observeViewModel()

        // Carga inicial a través del ViewModel
        viewModel.fetchExercises()
    }

    private fun setupViewModel() {
        val db = AppDataBase.getInstance(requireContext())

        // Configuración centralizada de Retrofit requerida por el Repositorio
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://wger.de/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
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
        viewModel = ViewModelProvider(this, factory)[ExercisesViewModel::class.java]
    }

    private fun initRecyclerView() {
        // Asumiendo que su adaptador acepta la lista y una acción lambda al dar clic en agregar
        adapter = ExerciseAdapter(exerciseList) { exercise ->
            // Aquí manejan la acción opcional de añadir un ejercicio a la sesión actual
        }
        binding.rvExercises.setHasFixedSize(true)
        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = adapter
    }

    private fun observeViewModel() {
        // Observador para la lista de ejercicios
        viewModel.exerciseList.observe(viewLifecycleOwner) { exercises ->
            exerciseList.clear()
            if (exercises.isNullOrEmpty()) {
                showError("No se encontraron ejercicios.")
            } else {
                exerciseList.addAll(exercises)
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                binding.rvExercises.visibility = View.VISIBLE
                binding.tvError.visibility = View.GONE
            }
        }

        // Observador para el estado de carga (Progress Bar)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvExercises.visibility = View.GONE
                binding.tvError.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        // Observador para los mensajes de error externos
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showError(message)
            }
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (query.isNotEmpty()) {
            // Filtrado local básico sobre la lista mutable mapeada por la API
            val filteredList = exerciseList.filter {
                it.name?.lowercase(Locale.ROOT)?.contains(query.lowercase(Locale.ROOT)) == true
            }

            if (filteredList.isNotEmpty()) {
                exerciseList.clear()
                exerciseList.addAll(filteredList)
                adapter.notifyDataSetChanged()
                binding.tvError.visibility = View.GONE
                binding.rvExercises.visibility = View.VISIBLE
            } else {
                showError("No se encontraron ejercicios locales para \"$query\"")
            }
            hideKeyboard()
        }
        return true
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvExercises.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message
    }

    private fun hideKeyboard() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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