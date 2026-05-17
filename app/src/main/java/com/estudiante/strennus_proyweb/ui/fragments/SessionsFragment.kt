package com.estudiante.strennus_proyweb.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
class SessionsFragment : Fragment() {

    companion object {
        fun newInstance(sesionId: Int) = SessionsFragment().apply {
            arguments = Bundle().also { it.putInt("sesion_id", sesionId) }
        }
    }

}
