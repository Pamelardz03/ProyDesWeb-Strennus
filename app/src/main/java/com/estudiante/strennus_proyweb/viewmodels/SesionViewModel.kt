package com.estudiante.strennus_proyweb.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estudiante.strennus_proyweb.entities.DetalleSesion
import com.estudiante.strennus_proyweb.entities.Sesion
import com.estudiante.strennus_proyweb.repository.AppRepository
import kotlinx.coroutines.launch

class SessionViewModel(private val repository: AppRepository) : ViewModel() {

    private val _sesionActual = MutableLiveData<Sesion?>()
    val sesionActual: LiveData<Sesion?> get() = _sesionActual

    private val _ejercicios = MutableLiveData<List<DetalleSesion>>()
    val ejercicios: LiveData<List<DetalleSesion>> get() = _ejercicios

    fun cargarSesion(sesionId: Int) {
        repository.obtenerSesionPorId(sesionId).observeForever { sesion ->
            _sesionActual.value = sesion
        }
        repository.obtenerDetallePorSesion(sesionId).observeForever { detalles ->
            _ejercicios.value = detalles
        }
    }

    fun crearSesion(sesion: Sesion, onCreada: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertarSesion(sesion)
            onCreada(id)
        }
    }

    fun agregarEjercicio(detalle: DetalleSesion) {
        viewModelScope.launch { repository.insertarDetalle(detalle) }
    }

    fun editarEjercicio(detalle: DetalleSesion) {
        viewModelScope.launch { repository.actualizarDetalle(detalle) }
    }

    fun eliminarEjercicio(detalle: DetalleSesion) {
        viewModelScope.launch { repository.eliminarDetalle(detalle) }
    }

    fun finalizarSesion(sesion: Sesion, horaFin: Long) {
        viewModelScope.launch {
            val duracion = ((horaFin - sesion.horaInicio) / 60000).toInt()
            val sesionFinalizada = sesion.copy(horaFin = horaFin, duracionMinutos = duracion)
            repository.actualizarSesion(sesionFinalizada)
        }
    }
}