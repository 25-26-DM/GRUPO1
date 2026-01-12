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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventory.R
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale


object LoginDestination : NavigationDestination {
    override val route = "login"
    override val titleRes = R.string.app_name
}

/**
 * Entry route for Login screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navigateToHome: () -> Unit,
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier
) {
    val (errorMessage, setErrorMessage) = remember { mutableStateOf("") }
    val (isRegistering, setIsRegistering) = remember { mutableStateOf(false) }
    val (registerUsername, setRegisterUsername) = remember { mutableStateOf("") }
    val (registerPassword, setRegisterPassword) = remember { mutableStateOf("") }
    val (registerPasswordConfirm, setRegisterPasswordConfirm) = remember { mutableStateOf("") }

    Scaffold { innerPadding ->
        if (isRegistering) {
            RegisterBody(
                username = registerUsername,
                onUsernameChange = setRegisterUsername,
                password = registerPassword,
                onPasswordChange = setRegisterPassword,
                passwordConfirm = registerPasswordConfirm,
                onPasswordConfirmChange = setRegisterPasswordConfirm,
                errorMessage = errorMessage,
                onRegisterClick = {
                    if (registerPassword == registerPasswordConfirm) {
                        setErrorMessage("")
                        viewModel.register(
                            registerUsername,
                            registerPassword,
                            onSuccess = {
                                setIsRegistering(false)
                                setRegisterUsername("")
                                setRegisterPassword("")
                                setRegisterPasswordConfirm("")
                                navigateToHome()
                            },
                            onError = { error ->
                                setErrorMessage(error)
                            }
                        )
                    } else {
                        setErrorMessage("Las contraseñas no coinciden")
                    }
                },
                onBackToLoginClick = {
                    setIsRegistering(false)
                    setErrorMessage("")
                },
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LoginBody(
                username = viewModel.loginUiState.username,
                onUsernameChange = viewModel::updateUsername,
                password = viewModel.loginUiState.password,
                onPasswordChange = viewModel::updatePassword,
                errorMessage = errorMessage,
                onLoginClick = {
                    setErrorMessage("")
                    viewModel.login(
                        onSuccess = navigateToHome,
                        onError = { error ->
                            setErrorMessage(error)
                        }
                    )
                },
                onRegisterClick = {
                    setIsRegistering(true)
                    setErrorMessage("")
                },
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun LoginBody(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.padding_large))
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo placeholder
        Image(
            painter = painterResource(id = R.drawable.grupo1_logo),
            contentDescription = "Logo de la aplicación",
            modifier = Modifier
                .height(200.dp)
                .padding(bottom = dimensionResource(id = R.dimen.padding_large)),
            contentScale = ContentScale.Fit
        )


        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        TextButton(onClick = onRegisterClick) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}

@Composable
private fun RegisterBody(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordConfirm: String,
    onPasswordConfirmChange: (String) -> Unit,
    errorMessage: String,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.padding_large))
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedTextField(
            value = passwordConfirm,
            onValueChange = onPasswordConfirmChange,
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        TextButton(onClick = onBackToLoginClick) {
            Text("Volver al login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginBodyPreview() {
    InventoryTheme {
        LoginBody(
            username = "",
            onUsernameChange = {},
            password = "",
            onPasswordChange = {},
            errorMessage = "",
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}
