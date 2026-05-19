package com.estudiante.strennus_proyweb.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estudiante.strennus_proyweb.entities.Sesion
import com.estudiante.strennus_proyweb.entities.Usuario
import com.estudiante.strennus_proyweb.repository.AppRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    private val _sesiones = MutableLiveData<List<Sesion>>()
    val sesiones: LiveData<List<Sesion>> get() = _sesiones

    private val _usuario = MutableLiveData<Usuario>()
    val usuario: LiveData<Usuario> get() = _usuario

    fun cargarSesiones(usuarioId: Int) {
        viewModelScope.launch {
            repository.obtenerSesionesPorUsuario(usuarioId).observeForever { listaSesiones ->
                _sesiones.postValue(listaSesiones)
            }
        }
    }

    fun cargarUsuario(usuarioId: Int) {
        viewModelScope.launch {
           repository.obtenerUsuarioPorId(usuarioId).observeForever { datosUsuario ->
                _usuario.postValue(datosUsuario)
            }
        }
    }

    fun eliminarSesion(sesion: Sesion) {
        viewModelScope.launch {
            repository.eliminarSesion(sesion)
        }
    }
}