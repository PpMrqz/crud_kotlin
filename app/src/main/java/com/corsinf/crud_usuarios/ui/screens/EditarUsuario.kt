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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarUsuarioScreen(usuario: Usuario, navController: NavController, viewModel: UsuariosViewModel) {
    val nombres = remember { mutableStateOf(usuario.nombres) }
    val apellidos = remember { mutableStateOf(usuario.apellidos) }
    val email = remember { mutableStateOf(usuario.email) }
    val ruc = remember { mutableStateOf(usuario.ruc) }



    // Son los datos del formulario valido?
    val isFormValid = remember {
        derivedStateOf {
            nombres.value.isNotEmpty() &&
                    apellidos.value.isNotEmpty() &&
                    email.value.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")) &&
                    (ruc.value.matches(Regex("^[0-9]{10}\$")) || ruc.value.matches(Regex("^[0-9]{13}\$")))

        }
    }


    // Snackbar para mostrar mensajes breves
    val snackbarHostState = remember { SnackbarHostState() }

    // Manejo de mensajes del backend
    LaunchedEffect(Unit) {
        viewModel.uiEventUpdate.collect { event ->
            when (event) {
                is UIEventUpdate.UserUpdatedSuccess -> navController.popBackStack()
                is UIEventUpdate.Error -> {
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
                title = { Text("Editar Usuario") },
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
            // Campo de nombres
            OutlinedTextField(
                value = nombres.value,
                onValueChange = { nombres.value = it },
                label = { Text("Nombres") },
                modifier = Modifier.fillMaxWidth()
            )
            // Campo de apellidos
            OutlinedTextField(
                value = apellidos.value,
                onValueChange = { apellidos.value = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de email
            OutlinedTextField(
                value = email.value,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^[A-Za-z0-9+_.-@]+$"))) {
                        email.value = it
                    }
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = email.value.isNotEmpty() && !email.value.matches(
                    Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
                )
            )

            // Campo de CI/RUC
            OutlinedTextField(
                value = ruc.value,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\$"))) {
                        ruc.value = it
                    }
                },
                label = { Text("RUC/CI") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = ruc.value.isNotEmpty() && (
                        !ruc.value.matches(Regex("^[0-9]{10}\$")) && !ruc.value.matches(Regex("^[0-9]{13}\$"))
                        )
            )

            // Botón para editar usuario
            Button(
                onClick = {
                    if (isFormValid.value) {
                        val usuarioEditado = Usuario(
                            id = usuario.id,
                            nombres = nombres.value,
                            apellidos = apellidos.value,
                            email = email.value,
                            ruc = ruc.value,
                        )

                        // El resutado de esto sera que el backend enviara los mensajes
                        // descritos al inicio, en LaunchedEffect
                        viewModel.editarUsuario(
                            usuarioEditado
                        )
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Editar Usuario")
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


