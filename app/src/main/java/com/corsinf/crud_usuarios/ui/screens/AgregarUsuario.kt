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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarUsuarioScreen(navController: NavController, viewModel: UsuariosViewModel) {
    val nombres = remember { mutableStateOf("") }
    val apellidos = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val ruc = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombres.value,
                onValueChange = { nombres.value = it },
                label = { Text("Nombres") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apellidos.value,
                onValueChange = { apellidos.value = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ruc.value,
                onValueChange = { ruc.value = it },
                label = { Text("RUC/CI") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val nuevoUsuario = Usuario(
                        id = 0, // El ID lo asignará la base de datos
                        nombres = nombres.value,
                        apellidos = apellidos.value,
                        email = email.value,
                        ruc = ruc.value
                    )

                    viewModel.agregarUsuario(
                        nuevoUsuario,
                        onSuccess = { navController.popBackStack() },
                        onError = { /* Manejar error */ }
                    )
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Añadir Usuario")
            }
        }
    }
}