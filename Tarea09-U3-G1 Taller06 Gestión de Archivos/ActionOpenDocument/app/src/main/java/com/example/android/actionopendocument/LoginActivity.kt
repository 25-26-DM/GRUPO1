/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.actionopendocument

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity de login que valida credenciales y guarda información de sesión.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var errorMessage: TextView

    // Lista de usuarios válidos
    private val validUsers = setOf(
        "Jami",
        "Cajamarca",
        "Valle",
        "Borja",
        "Andino",
        "Quiguango",
        "Cruz"
    )

    // Contraseña común para todos los usuarios
    private val validPassword = "12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar vistas
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        errorMessage = findViewById(R.id.error_message)

        // Configurar botón de login
        loginButton.setOnClickListener {
            attemptLogin()
        }
    }

    /**
     * Intenta realizar el login con las credenciales ingresadas.
     */
    private fun attemptLogin() {
        // Ocultar mensaje de error previo
        errorMessage.visibility = View.GONE

        // Obtener valores de los campos
        val username = usernameInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString() ?: ""

        // Validar que los campos no estén vacíos
        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.login_error_empty_fields))
            return
        }

        // Validar credenciales
        if (validUsers.contains(username) && password == validPassword) {
            // Login exitoso - guardar información en SharedPreferences
            saveLoginInfo(username)
            
            // Navegar a MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cerrar LoginActivity para que no se pueda volver con el botón atrás
        } else {
            // Credenciales incorrectas
            showError(getString(R.string.login_error_invalid_credentials))
        }
    }

    /**
     * Guarda la información de login en SharedPreferences, manteniendo un historial.
     */
    private fun saveLoginInfo(username: String) {
        val sharedPreferences = getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE)
        
        // Obtener el contador actual de ingresos
        val loginCount = sharedPreferences.getInt(KEY_LOGIN_COUNT, 0)
        val nextIndex = loginCount + 1

        // Obtener fecha y hora actual
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        // Guardar en SharedPreferences con índices únicos para el historial
        sharedPreferences.edit {
            // Guardar el último (para compatibilidad con MainActivity)
            putString(KEY_USERNAME, username)
            putString(KEY_LOGIN_DATE, currentDate)
            putString(KEY_LOGIN_TIME, currentTime)
            putBoolean(KEY_IS_LOGGED_IN, true)

            // Guardar en el historial (para el reporte del usuario)
            putString("${KEY_USERNAME}_$nextIndex", username)
            putString("${KEY_LOGIN_DATE}_$nextIndex", currentDate)
            putString("${KEY_LOGIN_TIME}_$nextIndex", currentTime)
            
            // Incrementar contador
            putInt(KEY_LOGIN_COUNT, nextIndex)
        }
    }

    /**
     * Muestra un mensaje de error.
     */
    private fun showError(message: String) {
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
    }

    companion object {
        const val LOGIN_PREFS = "LoginPreferences"
        const val KEY_USERNAME = "username"
        const val KEY_LOGIN_DATE = "login_date"
        const val KEY_LOGIN_TIME = "login_time"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_LOGIN_COUNT = "login_count"
    }
}
