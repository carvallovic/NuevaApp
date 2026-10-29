package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nuevaapp.repository.EquipoRepository
import com.example.nuevaapp.repository.MedicionRepository

class AppViewModelFactory(
    private val equipoRepository: EquipoRepository,
    private val medicionRepository: MedicionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EquipoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EquipoViewModel(equipoRepository) as T
        }
        if (modelClass.isAssignableFrom(MedicionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicionViewModel(medicionRepository) as T
        }
        if (modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistorialViewModel(medicionRepository) as T
        }
        throw IllegalArgumentException("ViewModel Class Desconocida")
    }
}