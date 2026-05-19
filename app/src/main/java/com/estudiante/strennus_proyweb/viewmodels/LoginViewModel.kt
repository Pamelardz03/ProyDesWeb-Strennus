package com.estudiante.strennus_proyweb.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estudiante.strennus_proyweb.entities.Usuario
import com.estudiante.strennus_proyweb.repository.AppRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AppRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Usuario?>()
    val loginResult: LiveData<Usuario?> get() = _loginResult

    fun iniciarSesion(usuario: String, contrasenia: String) {
        viewModelScope.launch {
            val user = repository.verificarUsuario(usuario, contrasenia)
            _loginResult.postValue(user)
        }
    }

    fun insertarUsuarioTest(usuario: Usuario) {
        viewModelScope.launch {
            repository.insertarUsuario(usuario)
        }
    }
}