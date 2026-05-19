package com.estudiante.strennus_proyweb.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.DialogCreateExerciseBinding
import com.estudiante.strennus_proyweb.entities.EjercicioPersonalizado
import com.estudiante.strennus_proyweb.repository.AppRepository
import com.estudiante.strennus_proyweb.ui.adapters.EjercicioConSeries
import com.estudiante.strennus_proyweb.ui.adapters.SerieTemp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateExerciseDialog(
    private val onExerciseCreated: (EjercicioConSeries) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateExerciseBinding? = null
    private val binding get() = _binding!!

    private val usuarioId by lazy {
        requireContext()
            .getSharedPreferences("strenuus_prefs", Context.MODE_PRIVATE)
            .getInt("usuario_id", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener { dismiss() }

        binding.btnCreateExercise.setOnClickListener {
            val nombre = binding.etExerciseName.text.toString().trim()
            val categoria = binding.etExerciseCategory.text.toString().trim()
            val descripcion = binding.etExerciseMET.text.toString().trim()

            if (nombre.isEmpty()) {
                binding.etExerciseName.error = "Escribe un nombre"
                return@setOnClickListener
            }

            // Guardar en BD
            guardarEnBD(nombre, categoria, descripcion)

            // Agregar a la sesión actual
            val nombreCompleto = if (categoria.isNotEmpty()) "$nombre ($categoria)" else nombre
            val ejercicio = EjercicioConSeries(
                nombre = nombreCompleto,
                series = mutableListOf(SerieTemp())
            )

            onExerciseCreated(ejercicio)
            dismiss()
        }

        // Actualizar indicador de intensidad según MET
        binding.etExerciseMET.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val met = binding.etExerciseMET.text.toString().toFloatOrNull() ?: 0f
                val (texto, color) = when {
                    met < 4.0f -> Pair("Baja", android.graphics.Color.parseColor("#4CAF50"))
                    met <= 6.0f -> Pair("Media", android.graphics.Color.parseColor("#FFC107"))
                    else -> Pair("Alta", android.graphics.Color.parseColor("#E60000"))
                }
                binding.tvCurrentIntensity.text = texto
                binding.tvCurrentIntensity.setTextColor(color)
            }
        }
    }

    private fun guardarEnBD(nombre: String, categoria: String, descripcion: String) {
        if (usuarioId == -1) return

        val ejercicio = EjercicioPersonalizado(
            usuarioId = usuarioId,
            nombre = nombre,
            categoria = categoria.ifEmpty { null },
            descripcion = descripcion.ifEmpty { null }
        )

        val db = AppDataBase.getInstance(requireContext())
        val repository = AppRepository(
            db.usuarioDao(),
            db.sesionDao(),
            db.detalleDao(),
            db.rutinaDao(),
            db.ejercicioPersonalizadoDao()
        )

        CoroutineScope(Dispatchers.IO).launch {
            repository.insertarEjercicioPersonalizado(ejercicio)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}