package com.example.danhgiaphim.ui.admin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AdminHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun signOut() {
        authRepository.signOut()
        UserSession.uid = null
    }
}
