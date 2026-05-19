package com.estudiante.strennus_proyweb.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.estudiante.strennus_proyweb.databinding.DialogCreateExerciseBinding
import com.estudiante.strennus_proyweb.ui.adapters.EjercicioConSeries
import com.estudiante.strennus_proyweb.ui.adapters.SerieTemp

class CreateExerciseDialog(
    private val onExerciseCreated: (EjercicioConSeries) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateExerciseBinding? = null
    private val binding get() = _binding!!

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

            if (nombre.isEmpty()) {
                binding.etExerciseName.error = "Escribe un nombre"
                return@setOnClickListener
            }

            // Crear el ejercicio como EjercicioConSeries con una serie inicial
            // La categoría se guarda en el nombre para mostrarse igual que los de la API
            val nombreCompleto = if (categoria.isNotEmpty()) "$nombre ($categoria)" else nombre
            val ejercicio = EjercicioConSeries(
                nombre = nombreCompleto,
                series = mutableListOf(SerieTemp())
            )

            onExerciseCreated(ejercicio)
            dismiss()
        }

        // Actualizar el indicador de intensidad según el MET
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}