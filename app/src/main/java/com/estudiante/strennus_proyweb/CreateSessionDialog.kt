package com.estudiante.strennus_proyweb

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.strennus_proyweb.databinding.DialogCreateSessionBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

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
                        searchExercises(query)
                    }
                }
            }
        })
    }

    private fun initRecyclerView() {
        adapter = ExerciseAdapter(exerciseList)
        binding.rvAvailableExercises.setHasFixedSize(true)
        binding.rvAvailableExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAvailableExercises.adapter = adapter
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
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateSessionDialog", "Error: ${e.message}")
            }
        }
    }

    private fun searchExercises(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Usa searchExercises con el parámetro name
                val call = retrofit.create(APIService::class.java)
                    .searchExercises(name = query)

                withContext(Dispatchers.Main) {
                    if (call.isSuccessful) {
                        val exercises = call.body()?.exercises ?: emptyList<Exercise>()
                        exerciseList.clear()
                        exerciseList.addAll(exercises)
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateSessionDialog", "Error búsqueda: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}