package com.estudiante.strennus_proyweb.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.estudiante.strennus_proyweb.databinding.FragmentProfileBinding
import com.estudiante.strennus_proyweb.ui.LogInActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Datos del usuario desde SharedPreferences ya guardados
        val prefs = requireContext().getSharedPreferences("strenuus_prefs", android.content.Context.MODE_PRIVATE)
        binding.tvUserName.text = prefs.getString("usuario_name", "Usuario")
        binding.tvUserRole.text = "@" + prefs.getString("usuario_username", "")

        // Expandir y Cerrar Detalles
        binding.headerOnlinePrograms.setOnClickListener {
            if (binding.contentOnlinePrograms.visibility == View.GONE) {
                binding.contentOnlinePrograms.visibility = View.VISIBLE
            } else {
                binding.contentOnlinePrograms.visibility = View.GONE
            }
        }

        // Expandir y Cerrar Detalles
        binding.headerSocialMedia.setOnClickListener {
            if (binding.contentSocialMedia.visibility == View.GONE) {
                binding.contentSocialMedia.visibility = View.VISIBLE
            } else {
                binding.contentSocialMedia.visibility = View.GONE
            }
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), LogInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}