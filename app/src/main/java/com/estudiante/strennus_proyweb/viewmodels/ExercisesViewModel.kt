package com.estudiante.strennus_proyweb.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estudiante.strennus_proyweb.data.Exercise
import com.estudiante.strennus_proyweb.repository.AppRepository
import kotlinx.coroutines.launch

class ExercisesViewModel(private val repository: AppRepository) : ViewModel() {

    private val _exerciseList = MutableLiveData<List<Exercise>>()
    val exerciseList: LiveData<List<Exercise>> get() = _exerciseList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchExercises() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = repository.buscarEjercicios()
                if (response?.isSuccessful == true) {
                    val exercises = response.body()?.exercises ?: emptyList()
                    _exerciseList.postValue(exercises)
                } else {
                    _errorMessage.postValue("Error al cargar ejercicios")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error de conexión: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}