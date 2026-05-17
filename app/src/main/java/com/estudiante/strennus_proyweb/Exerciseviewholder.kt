package com.estudiante.strennus_proyweb

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.databinding.ItemExerciseBinding

class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemExerciseBinding.bind(view)

    fun bind(exercise: Exercise) {
        binding.tvExerciseName.text = exercise.name ?: "Sin nombre"
        binding.tvExerciseDescription.text = exercise.description ?: ""
        binding.tvExerciseInitial.text = exercise.name?.firstOrNull()?.uppercase() ?: "E"
    }
}