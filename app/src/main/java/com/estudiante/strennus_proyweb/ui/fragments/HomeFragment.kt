package com.estudiante.strennus_proyweb.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.FragmentHomeBinding
import com.estudiante.strennus_proyweb.repository.AppRepository
import com.estudiante.strennus_proyweb.ui.adapters.SesionAdapter
import com.estudiante.strennus_proyweb.viewmodels.AppViewModelFactory
import com.estudiante.strennus_proyweb.viewmodels.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var sesionAdapter: SesionAdapter

    private val usuarioId by lazy {
        requireContext().getSharedPreferences("strenuus_prefs", android.content.Context.MODE_PRIVATE)
            .getInt("usuario_id", 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeViewModel()

        binding.btnNewSession.setOnClickListener {
            val dialog = CreateSessionDialog()
            dialog.show(parentFragmentManager, "CreateSessionDialog")
        }
    }

    private fun setupViewModel() {
        val db = AppDataBase.getInstance(requireContext())
        val repository = AppRepository(
            db.usuarioDao(),
            db.sesionDao(),
            db.detalleDao(),
            db.rutinaDao()
        )
        val factory = AppViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        viewModel.cargarSesiones(usuarioId)
    }

    private fun setupRecyclerView() {
        sesionAdapter = SesionAdapter(
            onItemClick = { sesion ->
                val fragment = SessionDetailFragment.newInstance(sesion.id)
                parentFragmentManager.beginTransaction()
                    .replace(com.estudiante.strennus_proyweb.R.id.viewPager, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { sesion ->
                viewModel.eliminarSesion(sesion)
            }
        )

        binding.rvSessionsPreview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sesionAdapter
            setHasFixedSize(false)
        }
    }

    private fun observeViewModel() {
        viewModel.sesiones.observe(viewLifecycleOwner) { listaSesiones ->
            sesionAdapter.submitList(listaSesiones)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}