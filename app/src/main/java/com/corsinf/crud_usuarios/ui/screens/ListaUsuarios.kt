package com.corsinf.crud_usuarios.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel
import com.corsinf.crud_usuarios.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaUsuariosScreen(navController: NavController, viewModel: UsuariosViewModel = viewModel()) {
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorConexion.collectAsState()
    var currentPage by remember { mutableStateOf(1) }
    val isLastPage by viewModel.isLastPage.collectAsState()

    SearchBar(viewModel)

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
                        Button(onClick = { viewModel.repetirBusqueda() }) {
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
                                    navController.navigate(Screen.DetalleUsuario.createRoute(usuario.id))
                                }
                            )
                        }

                        // Mostrar progress al final cuando se está cargando más datos
                        if (isLoading && usuarios.isNotEmpty()) {
                            item {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        // Cargar siguiente página cuando se llega al final
                        if (!isLoading && !isLastPage) {
                            item {
                                LaunchedEffect(Unit) {
                                    currentPage++
                                    viewModel.buscarUsuariosConReintento(currentPage)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun SearchBar(viewModel: UsuariosViewModel) {
    var searchText by remember { mutableStateOf("") }
    var selectedField by remember { mutableStateOf("nombres") }

    Column(modifier = Modifier.padding(16.dp)) {
        // Fila de búsqueda
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar usuarios") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    viewModel.buscarUsuariosConReintento(
                        pagina = 1,
                        textoBusqueda = searchText,
                        campoBusqueda = selectedField
                    )
                }
            ) {
                Text("Buscar")
            }
        }

        // Fila de opciones de campo de búsqueda
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            listOf("Nombres", "Apellidos", "RUC/CI", "Email").forEach { field ->
                val isSelected = selectedField == field.replace("/", "_").lowercase()
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedField = field.replace("/", "_").lowercase()
                        // Opcional pa disparar búsqueda automáticamente al cambiar campo
                        // viewModel.buscarUsuariosConReintento(
                        // pagina = 1,
                        // textoBusqueda = searchText,
                        // campoBusqueda = selectedField
                        // )
                    },
                    label = { Text(field.replace("_", "/").capitalize()) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}