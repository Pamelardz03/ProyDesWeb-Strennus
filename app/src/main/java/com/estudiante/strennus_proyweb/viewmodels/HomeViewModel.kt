package com.estudiante.strennus_proyweb.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estudiante.strennus_proyweb.entities.Sesion
import com.estudiante.strennus_proyweb.entities.Usuario
import com.estudiante.strennus_proyweb.repository.AppRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    private lateinit var _sesiones: LiveData<List<Sesion>>
    val sesiones get() = _sesiones

    private lateinit var _usuario: LiveData<Usuario>
    val usuario get() = _usuario

    fun cargarSesiones(usuarioId: Int) {
        _sesiones = repository.obtenerSesionesPorUsuario(usuarioId)
    }


    fun cargarUsuario(usuarioId: Int) {
        _usuario = repository.obtenerUsuarioPorId(usuarioId)
    }

    fun eliminarSesion(sesion: Sesion) {
        viewModelScope.launch {
            repository.eliminarSesion(sesion)
        }
    }
}