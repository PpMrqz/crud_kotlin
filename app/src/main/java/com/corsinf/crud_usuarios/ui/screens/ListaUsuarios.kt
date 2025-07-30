package com.corsinf.crud_usuarios.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel
import com.corsinf.crud_usuarios.ui.navigation.usuariosGraph
import com.corsinf.crud_usuarios.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaUsuariosScreen(navController: NavController, viewModel: UsuariosViewModel = viewModel()) {
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorConexion.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lista de Usuarios") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    println("agregar usuario")
                    navController.navigate(Screen.AgregarUsuario.route)
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar usuario")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.reintentarCarga() }) {
                            Text("Reintentar")
                        }
                    }
                }

                usuarios.isEmpty() -> {
                    Text(
                        "No hay usuarios disponibles",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(usuarios) { usuario ->
                            ListItem(
                                headlineContent = { Text("${usuario.nombres} ${usuario.apellidos}") },
                                supportingContent = { Text(usuario.email) },
                                modifier = Modifier.clickable {
                                    println(usuario)
                                    navController.navigate(Screen.DetalleUsuario.createRoute(usuario.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}