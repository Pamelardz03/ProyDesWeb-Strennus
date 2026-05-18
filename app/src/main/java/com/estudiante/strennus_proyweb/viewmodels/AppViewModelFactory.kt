package com.estudiante.strennus_proyweb.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estudiante.strennus_proyweb.repository.AppRepository
/*
class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(repository) as T
            modelClass.isAssignableFrom(SessionViewModel::class.java) ->
                @Suppress("UNCHECKED_CAST")
                SessionViewModel(repository) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
*/