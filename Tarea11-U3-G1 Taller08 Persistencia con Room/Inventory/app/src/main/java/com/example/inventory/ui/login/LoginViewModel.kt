/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.User
import com.example.inventory.data.UsersRepository
import kotlinx.coroutines.launch

/**
 * ViewModel to handle login operations
 */
class LoginViewModel(private val usersRepository: UsersRepository) : ViewModel() {

    var loginUiState by mutableStateOf(LoginUiState())
        private set

    fun updateUsername(username: String) {
        loginUiState = loginUiState.copy(username = username)
    }

    fun updatePassword(password: String) {
        loginUiState = loginUiState.copy(password = password)
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = usersRepository.getUserByCredentials(
                loginUiState.username,
                loginUiState.password
            )
            if (user != null) {
                loginUiState = loginUiState.copy(currentUser = user)
                onSuccess()
            } else {
                onError("Usuario o contraseña inválidos")
            }
        }
    }

    fun register(username: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val newUser = User(username = username, password = password)
                usersRepository.insertUser(newUser)
                loginUiState = loginUiState.copy(currentUser = newUser)
                onSuccess()
            } catch (e: Exception) {
                onError("Error al registrar: ${e.message}")
            }
        }
    }

    fun logout() {
        loginUiState = loginUiState.copy(currentUser = null)
    }
}

/**
 * Ui State for LoginScreen
 */
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val currentUser: User? = null
)
