package com.estudiante.strennus_proyweb.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.FragmentSessionsBinding
import com.estudiante.strennus_proyweb.entities.Sesion
import com.estudiante.strennus_proyweb.repository.AppRepository
import com.estudiante.strennus_proyweb.ui.adapters.SesionAdapter
import com.estudiante.strennus_proyweb.viewmodels.AppViewModelFactory
import com.estudiante.strennus_proyweb.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionsFragment : Fragment() {

    private var _binding: FragmentSessionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var sesionAdapter: SesionAdapter

    private val usuarioId by lazy {
        requireContext()
            .getSharedPreferences("strenuus_prefs", Context.MODE_PRIVATE)
            .getInt("usuario_id", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupViewModel() {
        val db = AppDataBase.getInstance(requireContext())

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://wger.de/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(com.estudiante.strennus_proyweb.data.APIService::class.java)

        val repository = AppRepository(
            db.usuarioDao(),
            db.sesionDao(),
            db.detalleDao(),
            db.rutinaDao(),
            apiService
        )
        val factory = AppViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        viewModel.cargarSesiones(usuarioId)
    }

    private fun setupRecyclerView() {
        sesionAdapter = SesionAdapter(
            onItemClick = { sesion ->
                val detailFragment = SessionDetailFragment.newInstance(sesion.id)
                parentFragmentManager.beginTransaction()
                    .replace(com.estudiante.strennus_proyweb.R.id.viewPager, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { sesion ->
                viewModel.eliminarSesion(sesion)
            }
        )
        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSessions.adapter = sesionAdapter
        binding.rvSessions.setHasFixedSize(false)
    }

    private fun observeViewModel() {
        viewModel.sesiones.observe(viewLifecycleOwner) { listaSesiones ->
            if (listaSesiones.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvSessions.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvSessions.visibility = View.VISIBLE
                sesionAdapter.submitList(listaSesiones)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(sesionId: Int) = SessionsFragment().apply {
            arguments = Bundle().also { it.putInt("sesion_id", sesionId) }
        }
    }
}