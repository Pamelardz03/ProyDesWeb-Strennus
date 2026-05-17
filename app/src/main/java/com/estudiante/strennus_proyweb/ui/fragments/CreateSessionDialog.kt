package com.estudiante.strennus_proyweb.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.data.APIService
import com.estudiante.strennus_proyweb.data.Exercise
import com.estudiante.strennus_proyweb.databinding.DialogCreateSessionBinding
import com.estudiante.strennus_proyweb.ui.adapters.ExerciseAdapter
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

    private lateinit var adapter: ExerciseAdapter
    private val exerciseList = mutableListOf<Exercise>()

    private val allExercises = mutableListOf<Exercise>()
    private var currentOffset = 0
    private var isLoading = false

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

        initRecyclerView()
        loadExercises()

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.etSearchExercise.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    if (query.isEmpty()) {
                        loadExercises()
                    } else {
                        filterExercises(query)
                    }
                }
            }
        })
    }

    private fun initRecyclerView() {
        adapter = ExerciseAdapter(exerciseList) { exercise ->
            // Por ahora solo muestra que se agregó
            android.widget.Toast.makeText(
                requireContext(),
                "${exercise.name} agregado",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        binding.rvAvailableExercises.setHasFixedSize(true)
        binding.rvAvailableExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAvailableExercises.adapter = adapter
        binding.rvAvailableExercises.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount
                if (lastVisible >= total - 3 && !isLoading) {
                    loadMoreExercises()
                }
            }
        })
    }

    private fun loadExercises() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = retrofit.create(APIService::class.java)
                    .getExercises()

                Log.d("CreateSessionDialog", "Code: ${call.code()}")
                Log.d("CreateSessionDialog", "Body: ${call.body()}")

                withContext(Dispatchers.Main) {
                    if (call.isSuccessful) {
                        val exercises = call.body()?.exercises ?: emptyList<Exercise>()
                        exerciseList.clear()
                        exerciseList.addAll(exercises)
                        allExercises.clear()
                        allExercises.addAll(exercises)
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateSessionDialog", "Error: ${e.message}")
            }
        }
    }

    private fun filterExercises(query: String) {
        val filtered = allExercises.filter {
            it.name.lowercase().contains(query.lowercase())
        }
        exerciseList.clear()
        exerciseList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun loadMoreExercises() {
        if (isLoading) return
        isLoading = true
        currentOffset += 100

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = retrofit.create(APIService::class.java)
                    .getExercises(offset = currentOffset)

                withContext(Dispatchers.Main) {
                    if (call.isSuccessful) {
                        val exercises = call.body()?.exercises ?: emptyList<Exercise>()
                        exerciseList.addAll(exercises)
                        allExercises.addAll(exercises)
                        adapter.notifyDataSetChanged()
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