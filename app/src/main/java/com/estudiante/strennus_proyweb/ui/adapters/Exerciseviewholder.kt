package com.estudiante.strennus_proyweb.ui.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.data.Exercise
import com.estudiante.strennus_proyweb.databinding.ItemExerciseBinding
import android.text.Html

class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemExerciseBinding.bind(view)

    fun bind(exercise: Exercise, onAddClick: (Exercise) -> Unit) {
        binding.tvExerciseName.text = exercise.name ?: "Sin nombre"
        binding.tvExerciseDescription.text = Html.fromHtml(
            exercise.description ?: "",
            Html.FROM_HTML_MODE_COMPACT
        ).toString().trim()

        binding.btnAddExercise.setOnClickListener {
            onAddClick(exercise)
        }
    }
}