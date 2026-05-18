package com.estudiante.strennus_proyweb.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.R
import com.estudiante.strennus_proyweb.databinding.ItemDetalleBinding
import com.estudiante.strennus_proyweb.entities.DetalleSesion

class DetalleAdapter : ListAdapter<DetalleSesion, DetalleAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemDetalleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(detalle: DetalleSesion) {
            binding.tvNombreEjercicio.text = detalle.nombreEjercicio
            val peso = if (detalle.peso != null) "• ${detalle.peso} kg" else ""
            binding.tvDetalle.text = "Serie ${detalle.series} • ${detalle.repeticiones} reps $peso"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetalleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<DetalleSesion>() {
        override fun areItemsTheSame(old: DetalleSesion, new: DetalleSesion) = old.id == new.id
        override fun areContentsTheSame(old: DetalleSesion, new: DetalleSesion) = old == new
    }
}