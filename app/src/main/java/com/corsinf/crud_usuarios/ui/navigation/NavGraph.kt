package com.corsinf.crud_usuarios.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.corsinf.crud_usuarios.ui.screens.ListaUsuariosScreen
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel

sealed class Screen(val route: String) {
    object ListaUsuarios : Screen("lista_usuarios")
    object DetalleUsuario : Screen("detalle_usuario") {
        const val USER_ID = "user_id"
        fun createRoute(userId: Int) = "detalle_usuario/$userId"
    }
    object AgregarUsuario : Screen("agregar_usuario")
}

fun NavGraphBuilder.usuariosGraph(navController: NavController, viewModel: UsuariosViewModel) {
    navigation(
        startDestination = Screen.ListaUsuarios.route,
        route = "usuarios"
    ) {
        composable(Screen.ListaUsuarios.route) {
            ListaUsuariosScreen(navController, viewModel)
        }
    }
}