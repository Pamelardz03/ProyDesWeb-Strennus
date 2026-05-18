package com.estudiante.strennus_proyweb.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.R
import com.estudiante.strennus_proyweb.databinding.ItemSelectedExerciseBinding

data class SerieTemp(
    var reps: Int = 12,
    var peso: Float? = null
)

data class EjercicioConSeries(
    val nombre: String,
    val series: MutableList<SerieTemp> = mutableListOf(SerieTemp()),
    var isExpanded: Boolean = true  // true = editando, false = guardado/colapsado
)

class SelectedExerciseAdapter(
    private val ejercicios: MutableList<EjercicioConSeries>,
    private val onRemoveEjercicio: (EjercicioConSeries) -> Unit
) : RecyclerView.Adapter<SelectedExerciseAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSelectedExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ejercicio: EjercicioConSeries) {
            binding.tvExerciseName.text = ejercicio.nombre
            updateSeriesCount(ejercicio)
            applyExpandedState(ejercicio)

            // Botón Guardar ejercicio — colapsa el cuadro
            binding.btnSaveExercise.setOnClickListener {
                ejercicio.isExpanded = false
                applyExpandedState(ejercicio)
            }

            // Botón Editar — despliega el cuadro de nuevo
            binding.btnEditExercise.setOnClickListener {
                ejercicio.isExpanded = true
                applyExpandedState(ejercicio)
            }

            // Eliminar ejercicio completo
            binding.btnRemoveExercise.setOnClickListener {
                onRemoveEjercicio(ejercicio)
            }

            // Botón + Nueva serie
            binding.btnAddSerie.setOnClickListener {
                ejercicio.series.add(SerieTemp())
                renderSeriesRows(ejercicio)
                updateSeriesCount(ejercicio)
            }

            renderSeriesRows(ejercicio)
        }

        private fun applyExpandedState(ejercicio: EjercicioConSeries) {
            if (ejercicio.isExpanded) {
                // Expandido: mostrando series para editar
                binding.layoutSeriesContainer.visibility = View.VISIBLE
                binding.btnEditExercise.visibility = View.GONE
            } else {
                // Colapsado: guardado, solo muestra nombre + contador + editar
                binding.layoutSeriesContainer.visibility = View.GONE
                binding.btnEditExercise.visibility = View.VISIBLE
            }
            updateSeriesCount(ejercicio)
        }

        private fun updateSeriesCount(ejercicio: EjercicioConSeries) {
            binding.tvSeriesCount.text = "${ejercicio.series.size} series"
        }

        private fun renderSeriesRows(ejercicio: EjercicioConSeries) {
            val container = binding.llSeriesRows
            container.removeAllViews()
            val inflater = LayoutInflater.from(container.context)

            ejercicio.series.forEachIndexed { index, serie ->
                val rowView = inflater.inflate(R.layout.item_serie_row, container, false)

                rowView.findViewById<TextView>(R.id.tvSerieNumber).text = "${index + 1}"

                val etReps = rowView.findViewById<EditText>(R.id.etReps)
                etReps.setText(serie.reps.toString())
                etReps.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) serie.reps = etReps.text.toString().toIntOrNull() ?: 12
                }

                val etPeso = rowView.findViewById<EditText>(R.id.etPeso)
                etPeso.setText(serie.peso?.toString() ?: "")
                etPeso.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) serie.peso = etPeso.text.toString().toFloatOrNull()
                }

                val btnDelete = rowView.findViewById<ImageButton>(R.id.btnDeleteSerie)
                btnDelete.visibility = if (ejercicio.series.size > 1) View.VISIBLE else View.GONE
                btnDelete.setOnClickListener {
                    ejercicio.series.removeAt(index)
                    renderSeriesRows(ejercicio)
                    updateSeriesCount(ejercicio)
                }

                container.addView(rowView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectedExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ejercicios[position])
    }

    override fun getItemCount() = ejercicios.size
}