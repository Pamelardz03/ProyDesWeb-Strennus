package com.estudiante.strennus_proyweb

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(val exercises: List<Exercise>) :
    RecyclerView.Adapter<ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ExerciseViewHolder(
            layoutInflater.inflate(R.layout.item_exercise, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val item = exercises[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = exercises.size
}