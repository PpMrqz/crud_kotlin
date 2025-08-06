package com.corsinf.crud_usuarios.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.corsinf.crud_usuarios.data.Usuario
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel.UIEventDelete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.corsinf.crud_usuarios.data.MsgExito
import com.corsinf.crud_usuarios.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleUsuarioScreen(usuario: Usuario, navController: NavController, viewModel: UsuariosViewModel) {

    // Snackbar para mostrar mensajes breves
    val snackbarHostState = remember { SnackbarHostState() }

    // Manejo de mensajes del backend
    LaunchedEffect(Unit) {
        viewModel.uiEventDelete.collect { event ->
            when (event) {
                is UIEventDelete.UserDeletedSuccess -> {
                    navController.popBackStack()
                    snackbarHostState.showSnackbar(
                        message = MsgExito.USUARIO_ELIMINADO,
                        duration = SnackbarDuration.Short
                    )
                }
                is UIEventDelete.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Estado para controlar la visibilidad del diálogo de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Diálogo de confirmación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro que deseas eliminar este usuario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.eliminarUsuario(usuario)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    // Menú desplegable de edición
                    var expanded by remember { mutableStateOf(false) }

                    Box {
                        IconButton(
                            onClick = { expanded = true }
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Opciones de edición")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar datos") },
                                onClick = {
                                    expanded = false
                                    navController.navigate(Screen.EditarUsuario.createRoute(usuario.id))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cambiar contraseña") },
                                onClick = {
                                    expanded = false
                                    navController.navigate(Screen.CambiarContrasenaUsuario.createRoute(usuario.id))
                                }
                            )
                        }
                    }

                    // Botón de eliminar
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar usuario")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Nombres: ${usuario.nombres}")
            Text(text = "Apellidos: ${usuario.apellidos}")
            Text(text = "Email: ${usuario.email}")
            Text(text = "RUC/CI: ${usuario.ruc}")
        }
    }
}