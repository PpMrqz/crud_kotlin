package com.corsinf.crud_usuarios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel
import com.corsinf.crud_usuarios.ui.navigation.Screen
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel.UIEventListMsg
import coil.compose.AsyncImage
import com.corsinf.crud_usuarios.R
import com.corsinf.crud_usuarios.ui.components.CustomOutlinedTextField
import com.corsinf.crud_usuarios.ui.theme.AzulVivo
import com.corsinf.crud_usuarios.ui.theme.Blanco
import com.corsinf.crud_usuarios.ui.theme.GrisClaro
import com.corsinf.crud_usuarios.ui.theme.GrisOscuro
import com.corsinf.crud_usuarios.ui.theme.Negro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaUsuariosScreen(navController: NavController, viewModel: UsuariosViewModel = viewModel()) {
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorConexion.collectAsState()
    val isLastPage by viewModel.isLastPage.collectAsState()
    var currentPage by viewModel.currentPage
    var searchText by viewModel.searchText
    var selectedSearchField by viewModel.selectedSearchField
    var isSearch by viewModel.isSearch

    // Estado para el LazyColumn
    val listState = rememberLazyListState()

    // Snackbar para mostrar mensajes breves
    val snackbarHostState = remember { SnackbarHostState() }



    LaunchedEffect(usuarios.size) {
        // Efecto para manejar el scroll cuando se añaden nuevos items
        if (usuarios.size > 20 && currentPage > 1) {
            // Mantener la posición actual después de cargar más items
            val currentItem = listState.firstVisibleItemIndex
            val currentScrollOffset = listState.firstVisibleItemScrollOffset
            listState.scrollToItem(currentItem, currentScrollOffset)
        }

        // Manejo de mensajes del backend
        viewModel.uiEventListMsg.collect { event ->
            when (event) {
                is UIEventListMsg.Success -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }

                is UIEventListMsg.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Lista de Usuarios") })
                Column(modifier = Modifier.padding(16.dp)) {
                    // Fila de búsqueda
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CustomOutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = "Buscar usuarios",
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                isSearch = true
                                currentPage = 1
                                viewModel.limpiarBusquedaAnterior()
                                viewModel.buscarUsuariosConReintento()
                            }
                        ) {
                            Text("Buscar")
                        }
                    }

                    // Fila de opciones de campo de búsqueda
                    Row(
                        horizontalArrangement = Arrangement.Absolute.Left,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                            listOf("Nombre", "CI/RUC", "Email").forEach { field ->
                                val isSelected = selectedSearchField == field.replace("/", "_").lowercase()
                                FilterChip(
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    selected = isSelected,
                                    onClick = {
                                        selectedSearchField = field.replace("/", "_").lowercase()
                                    },
                                    label = { Text(field.replace("_", "/").capitalize()) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        if (isSearch) {
                            Button(
                                onClick = {
                                    isSearch = false
                                    currentPage = 1
                                    searchText = ""
                                    selectedSearchField = "nombre"
                                    viewModel.limpiarBusquedaAnterior()
                                    viewModel.buscarUsuariosConReintento()
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            }
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(usuarios) { usuario ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(Screen.DetalleUsuario.createRoute(usuario.id))
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Foto de perfil
                                    AsyncImage(
                                        model = usuario.foto_url,
                                        contentDescription = "Foto de perfil de ${usuario.nombres}",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        placeholder = painterResource(R.drawable.index),
                                        error = painterResource(R.drawable.index),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = "${usuario.nombres} ${usuario.apellidos}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AzulVivo
                                        )
                                        Text(
                                            text = usuario.email,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GrisOscuro
                                        )
                                    }
                                }
                            }
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

                        if (!isLoading && !isLastPage && usuarios.isNotEmpty()) {
                            item {
                                LaunchedEffect(Unit) {
                                    currentPage++
                                    if (isSearch) {
                                        viewModel.buscarUsuariosConReintento()

                                    } else {
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

}
