package com.corsinf.crud_usuarios.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.corsinf.crud_usuarios.ui.screens.AgregarUsuarioScreen
import com.corsinf.crud_usuarios.ui.screens.DetalleUsuarioScreen
import com.corsinf.crud_usuarios.ui.screens.ListaUsuariosScreen
import com.corsinf.crud_usuarios.ui.screens.EditarUsuarioScreen
import com.corsinf.crud_usuarios.ui.screens.CambiarContrasenaUsuarioScreen
import com.corsinf.crud_usuarios.ui.screens.LoginScreen
import com.corsinf.crud_usuarios.viewmodels.UsuariosViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ListaUsuarios : Screen("lista_usuarios")
    object DetalleUsuario : Screen("detalle_usuario") {
        const val USER_ID = "user_id"
        fun createRoute(userId: Int) = "detalle_usuario/$userId"
    }
    object AgregarUsuario : Screen("agregar_usuario")
    object EditarUsuario : Screen("editar_usuario") {
        const val USER_ID = "user_id"
        fun createRoute(userId: Int) = "editar_usuario/$userId"
    }

    object CambiarContrasenaUsuario : Screen("cambiar_contrasena_usuario") {
        const val USER_ID = "user_id"
        fun createRoute(userId: Int) = "cambiar_contrasena_usuario/$userId"
    }
}
fun NavGraphBuilder.appGraph(navController: NavController, viewModel: UsuariosViewModel) {
    navigation(
        startDestination = Screen.Login.route,
        route = "app"
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        usuariosGraph(navController, viewModel)
    }
}

fun NavGraphBuilder.usuariosGraph(navController: NavController, viewModel: UsuariosViewModel) {
    navigation(
        startDestination = Screen.ListaUsuarios.route,
        route = "usuarios"
    ) {
        composable(Screen.ListaUsuarios.route) {
            ListaUsuariosScreen(navController, viewModel)
        }
        composable(Screen.AgregarUsuario.route) {
            AgregarUsuarioScreen(navController, viewModel)
        }
        composable(
            "${Screen.DetalleUsuario.route}/{${Screen.DetalleUsuario.USER_ID}}"
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screen.DetalleUsuario.USER_ID)?.toIntOrNull()
            if (userId != null) {
                val usuario = viewModel.getUsuarioById(userId)
                if (usuario != null) {
                    DetalleUsuarioScreen(usuario, navController, viewModel)
                } else {
                    println("Usuario es null, no se puede navegar a ventana de detalles")
                }
            }

        }

        composable(
            "${Screen.EditarUsuario.route}/{${Screen.EditarUsuario.USER_ID}}"
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screen.EditarUsuario.USER_ID)?.toIntOrNull()
            if (userId != null) {
                val usuario = viewModel.getUsuarioById(userId)
                if (usuario != null) {
                    EditarUsuarioScreen(usuario, navController, viewModel)
                } else {
                    println("Usuario es null, no se puede navegar a ventana de editar usuario")
                }
            }
        }

        composable(
            "${Screen.CambiarContrasenaUsuario.route}/{${Screen.CambiarContrasenaUsuario.USER_ID}}"
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screen.EditarUsuario.USER_ID)?.toIntOrNull()
            if (userId != null) {
                val usuario = viewModel.getUsuarioById(userId)
                if (usuario != null) {
                    CambiarContrasenaUsuarioScreen(usuario, navController, viewModel)
                } else {
                    println("Usuario es null, no se puede navegar a ventana de cambiar contraseña de usuario")
                }
            }
        }
    }
}