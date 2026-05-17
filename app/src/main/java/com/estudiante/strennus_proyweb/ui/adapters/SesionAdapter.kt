package com.estudiante.strennus_proyweb.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.R
import com.estudiante.strennus_proyweb.databinding.ItemSessionBinding
import com.estudiante.strennus_proyweb.entities.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SesionAdapter(
    private val onItemClick: (Sesion) -> Unit,
    private val onDeleteClick: (Sesion) -> Unit
) : ListAdapter<Sesion, SesionAdapter.SesionViewHolder>(SesionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SesionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SesionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SesionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SesionViewHolder(
        private val binding: ItemSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sesion: Sesion) {
            // Formato de fecha legible
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val fechaStr = sdf.format(Date(sesion.fecha))

            binding.tvSessionName.text = "Sesión del $fechaStr"

            // Detalles: duración si está finalizada, o "En progreso"
            val detalles = if (sesion.duracionMinutos != null) {
                "${sesion.duracionMinutos} min • $fechaStr"
            } else {
                "En progreso • $fechaStr"
            }
            binding.tvSessionDetails.text = detalles

            binding.root.setOnClickListener { onItemClick(sesion) }
        }
    }

    class SesionDiffCallback : DiffUtil.ItemCallback<Sesion>() {
        override fun areItemsTheSame(oldItem: Sesion, newItem: Sesion) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Sesion, newItem: Sesion) = oldItem == newItem
    }
}
