package com.estudiante.strennus_proyweb.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.strennus_proyweb.DAO.DetalleSesionDao
import com.estudiante.strennus_proyweb.DAO.RutinaDao
import com.estudiante.strennus_proyweb.DAO.SesionDao
import com.estudiante.strennus_proyweb.DAO.UsuarioDao
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.FragmentHomeBinding
import com.estudiante.strennus_proyweb.repository.AppRepository
import com.estudiante.strennus_proyweb.ui.adapters.SesionAdapter
//import com.estudiante.strennus_proyweb.viewmodels.AppViewModelFactory
import com.estudiante.strennus_proyweb.viewmodels.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var sesionAdapter: SesionAdapter

    // ID del usuario logueado — en un proyecto real vendría de SharedPreferences/Session
    private val usuarioId = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setupViewModel()
        setupRecyclerView()
        //observeViewModel()

        // Abrir el diálogo de nueva sesión
        binding.btnNewSession.setOnClickListener {
            val dialog = CreateSessionDialog()
            dialog.show(parentFragmentManager, "CreateSessionDialog")
        }
    }
/*
    private fun setupViewModel() {
        val db = AppDataBase.getInstance(requireContext())
        val repository = AppRepository(
            db.usuarioDao(),
            db.sesionDao(),
            db.detalleDao(),
            db.rutinaDao()
        )
        val factory = com.estudiante.strennus_proyweb.viewmodels.AppViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        viewModel.cargarSesiones(usuarioId)
    }

 */

    private fun setupRecyclerView() {
        sesionAdapter = SesionAdapter(
            onItemClick = { sesion ->
                // Navegar a SessionsFragment pasando el id de la sesión
                //val fragment = SessionsFragment.newInstance(sesion.id)
                //parentFragmentManager.beginTransaction()
                    //.replace(com.estudiante.strennus_proyweb.R.id.viewPager, fragment)
                    //.addToBackStack(null)
                    //.commit()
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
/*
    private fun observeViewModel() {
        // observe() conecta el LiveData al ciclo de vida del Fragment
        viewModel.sesiones.observe(viewLifecycleOwner) { listaSesiones ->
            sesionAdapter.submitList(listaSesiones)
        }
    }

 */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}


