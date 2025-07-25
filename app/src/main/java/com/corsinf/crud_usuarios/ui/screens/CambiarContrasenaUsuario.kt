package com.corsinf.crud_usuarios.ui.screens


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.corsinf.crud_usuarios.data.Usuario
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel.UIEventAdd
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel.UIEventUpdate
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel.UIEventUpdatePass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarContrasenaUsuarioScreen(usuario: Usuario, navController: NavController, viewModel: UsuariosViewModel) {


    // Estado para los campos de contraseña
    val contrasena = remember { mutableStateOf("") }
    val confirmarContrasena = remember { mutableStateOf("") }
    val contrasenaVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }

    // Validaciones de la contraseña
    val isPasswordValid = remember {
        derivedStateOf {
            contrasena.value.length >= 8 &&
                    contrasena.value.matches(Regex(".*[A-Z].*")) && // Al menos 1 mayúscula
                    contrasena.value.matches(Regex(".*[a-z].*")) && // Al menos 1 minúscula
                    contrasena.value.matches(Regex(".*\\d.*")) &&   // Al menos 1 número
                    contrasena.value.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) // Al menos 1 caracter especial
        }
    }
    val doPasswordsMatch = remember {
        derivedStateOf {
            contrasena.value.isNotEmpty() &&
                    confirmarContrasena.value.isNotEmpty() &&
                    contrasena.value == confirmarContrasena.value
        }
    }


    // Son los datos del formulario valido?
    val isFormValid = remember {
        derivedStateOf {
            isPasswordValid.value &&
                    doPasswordsMatch.value
        }
    }


    // Snackbar para mostrar mensajes breves
    val snackbarHostState = remember { SnackbarHostState() }

    // Manejo de mensajes del backend
    LaunchedEffect(Unit) {
        viewModel.uiEventUpdatePass.collect { event ->
            when (event) {
                is UIEventUpdatePass.UserUpdatedPassSuccess -> navController.popBackStack()
                is UIEventUpdatePass.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar contraseña de usuario:") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(text = "Nombres: ${usuario.nombres}")
            Text(text = "Apellidos: ${usuario.apellidos}")
            Text(text = "Email: ${usuario.email}")
            Text(text = "RUC/CI: ${usuario.ruc}")

            // Campo de contraseña
            OutlinedTextField(
                value = contrasena.value,
                onValueChange = { contrasena.value = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (contrasenaVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { contrasenaVisible.value = !contrasenaVisible.value }) {
                        Icon(
                            imageVector = if (contrasenaVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (contrasenaVisible.value) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = contrasena.value.isNotEmpty() && !isPasswordValid.value
            )
            // Mensaje de que nomás debe tener la contraseña
            if (contrasena.value.isNotEmpty() && !isPasswordValid.value) {
                Text(
                    text = "La contraseña debe tener:\n- Mínimo 8 caracteres\n- Mayúsculas y minúsculas\n- Números\n- Caracteres especiales",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
                )
            }


            // Campo de confirmar contraseña
            OutlinedTextField(
                value = confirmarContrasena.value,
                onValueChange = { confirmarContrasena.value = it },
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = {
                        confirmPasswordVisible.value = !confirmPasswordVisible.value
                    }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible.value) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = confirmarContrasena.value.isNotEmpty() && !doPasswordsMatch.value
            )
            // Mensaje de que las contraseñas no coinciden
            if (confirmarContrasena.value.isNotEmpty() && !doPasswordsMatch.value) {
                Text(
                    text = "Las contraseñas no coinciden",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
                )
            }

            // Botón para editar usuario
            Button(
                onClick = {
                    if (isFormValid.value) {
                        val usuarioEditado = Usuario(
                            id = usuario.id,
                            nombres = usuario.nombres,
                            apellidos = usuario.apellidos,
                            email = usuario.email,
                            ruc = usuario.ruc,
                            contrasena = contrasena.value,
                        )

                        // El resutado de esto sera que el backend enviara los mensajes
                        // descritos al inicio, en LaunchedEffect
                        viewModel.cambiarContrasenaUsuario(
                            usuarioEditado
                        )
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cambiar Contraseña")
            }

            // Mensaje de que falta llenar algo
            if (!isFormValid.value) {
                Text(
                    text = "Complete todos los campos correctamente",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}


