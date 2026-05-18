package com.estudiante.strennus_proyweb.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.estudiante.strennus_proyweb.R
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.FragmentSessionsDetailBinding
import com.estudiante.strennus_proyweb.entities.DetalleSesion
import com.estudiante.strennus_proyweb.repository.AppRepository
import com.estudiante.strennus_proyweb.viewmodels.AppViewModelFactory
import com.estudiante.strennus_proyweb.viewmodels.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionDetailFragment : Fragment() {

    private var _binding: FragmentSessionsDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SessionViewModel
    private val sesionId by lazy { arguments?.getInt("sesion_id") ?: -1 }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        observeViewModel()
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun setupViewModel() {
        val db = AppDataBase.getInstance(requireContext())
        val repository = AppRepository(
            db.usuarioDao(), db.sesionDao(), db.detalleDao(), db.rutinaDao()
        )
        val factory = AppViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SessionViewModel::class.java]
        viewModel.cargarSesion(sesionId)
    }

    private fun observeViewModel() {
        viewModel.sesionActual.observe(viewLifecycleOwner) { sesion ->
            sesion?.let {
                binding.tvSessionName.text = sesion.nombre
                binding.tvSessionDate.text = SimpleDateFormat(
                    "dd MMM yyyy", Locale.getDefault()
                ).format(Date(sesion.fecha))
                binding.tvSessionDuration.text = if (sesion.duracionMinutos != null)
                    "${sesion.duracionMinutos} min" else "—"
            }
        }

        viewModel.ejercicios.observe(viewLifecycleOwner) { detalles ->
            renderEjerciciosAgrupados(detalles)
        }
    }

    // Agrupa los DetalleSesion por nombre de ejercicio y los muestra como tarjetas
    private fun renderEjerciciosAgrupados(detalles: List<DetalleSesion>) {
        binding.llEjercicios.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        // Agrupar por nombre de ejercicio
        val grupos = detalles.groupBy { it.nombreEjercicio }

        grupos.forEach { (nombreEjercicio, series) ->
            val cardView = inflater.inflate(R.layout.item_ejercicio_detalle, binding.llEjercicios, false)

            cardView.findViewById<TextView>(R.id.tvEjercicioNombre).text = nombreEjercicio
            cardView.findViewById<TextView>(R.id.tvSeriesCount).text = "${series.size} series"

            val llSeries = cardView.findViewById<LinearLayout>(R.id.llSeriesDetalle)
            val btnToggle = cardView.findViewById<ImageButton>(R.id.btnToggleDetalle)
            var isExpanded = false

            // Por defecto colapsado
            llSeries.visibility = View.GONE

            btnToggle.setOnClickListener {
                isExpanded = !isExpanded
                llSeries.visibility = if (isExpanded) View.VISIBLE else View.GONE
            }

            // Agregar filas de series
            series.forEachIndexed { index, detalle ->
                val rowView = inflater.inflate(R.layout.item_serie_row_readonly, llSeries, false)
                rowView.findViewById<TextView>(R.id.tvSerieNumber).text = "${index + 1}"
                rowView.findViewById<TextView>(R.id.tvReps).text = "${detalle.repeticiones} reps"
                rowView.findViewById<TextView>(R.id.tvPeso).text =
                    if (detalle.peso != null) "${detalle.peso} kg" else "— kg"
                llSeries.addView(rowView)
            }

            binding.llEjercicios.addView(cardView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(sesionId: Int) = SessionDetailFragment().apply {
            arguments = Bundle().also { it.putInt("sesion_id", sesionId) }
        }
    }
}