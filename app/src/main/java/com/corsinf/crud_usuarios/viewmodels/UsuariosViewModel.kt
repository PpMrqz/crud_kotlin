package com.corsinf.crud_usuarios.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corsinf.crud_usuarios.data.DatabaseHelper
import com.corsinf.crud_usuarios.data.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.sql.ResultSet
import java.security.MessageDigest

class UsuariosViewModel(private val context: Context) : ViewModel() {
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Eventos a enviar a travez para que sean recividos por la UIs que usen collect
    //Eventos agregar usuario
    private val _uiEventAdd = Channel<UIEventAdd>()
    val uiEventAdd = _uiEventAdd.receiveAsFlow()
    sealed class UIEventAdd {
        object UserAddedSuccess : UIEventAdd()
        data class Error(val message: String) : UIEventAdd()
    }
    // Eventos borrar usuario
    private val _uiEventDelete = Channel<UIEventDelete>()
    val uiEventDelete = _uiEventDelete.receiveAsFlow()
    sealed class UIEventDelete {
        object UserDeletedSuccess : UIEventDelete()
        data class Error(val message: String) : UIEventDelete()
    }
    // Eventos actuzalizar usuario
    private val _uiEventUpdate = Channel<UIEventUpdate>()
    val uiEventUpdate = _uiEventUpdate.receiveAsFlow()
    sealed class UIEventUpdate {
        object UserUpdatedSuccess : UIEventUpdate()
        data class Error(val message: String) : UIEventUpdate()
    }
    // Eventos cambiar contraseña de usuario
    private val _uiEventUpdatePass = Channel<UIEventUpdatePass>()
    val uiEventUpdatePass = _uiEventUpdatePass.receiveAsFlow()
    sealed class UIEventUpdatePass {
        object UserUpdatedPassSuccess : UIEventUpdatePass()
        data class Error(val message: String) : UIEventUpdatePass()
    }

    // El viewmodel cargara usuarios al iniciar para tenerlos disponibles desde el inicio
    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val dbHelper = DatabaseHelper(context)
            val connection = dbHelper.getConnection()
            val usuariosList = mutableListOf<Usuario>()
            println("CARGANDO USUARIOS")
            val query = """
                    SELECT [nombres], [apellidos], [id_usuarios], [email], [ci_ruc]
                    FROM [dbo].[USUARIOS]
                    """
            val statement = connection?.createStatement()
            try {
                val resultSet = statement?.executeQuery(query )
                while (resultSet?.next() == true) {
                    usuariosList.add(
                        Usuario(
                            id = resultSet.getInt("id_usuarios"),
                            nombres = resultSet.getString("nombres"),
                            apellidos = resultSet.getString("apellidos"),
                            email = resultSet.getString("email"),
                            ruc = resultSet.getString("ci_ruc")
                        )
                    )
                }

                _usuarios.value = usuariosList
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                statement?.close()
                dbHelper.closeConnection()
                _isLoading.value = false
            }
        }
    }

    fun agregarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val connection = dbHelper.getConnection()
            val query = """
                    INSERT INTO [dbo].[USUARIOS] 
                    ([nombres], [apellidos], [email], [ci_ruc], [password]) 
                    VALUES (?, ?, ?, ?, ?)
                """.trimIndent()

            val contrasenaHash = hashearString(usuario.contrasena)

            val preparedStatement = connection?.prepareStatement(query)

            try {
                preparedStatement?.setString(1, sanearString(usuario.nombres))
                preparedStatement?.setString(2, sanearString(usuario.apellidos))
                preparedStatement?.setString(3, sanearString(usuario.email))
                preparedStatement?.setString(4, sanearString(usuario.ruc))
                preparedStatement?.setString(5, contrasenaHash)

                var rowsAffected = 0
                rowsAffected = preparedStatement?.executeUpdate() ?: 0

                if (rowsAffected > 0) {
                    cargarUsuarios() // Recargar la lista
                    _uiEventAdd.send(UIEventAdd.UserAddedSuccess)
                } else {
                    _uiEventAdd.send(UIEventAdd.Error("No se pudo insertar el usuario"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEventAdd.send(UIEventAdd.Error(e.message ?: "Error desconocido"))
            } finally {
                preparedStatement?.close()
                dbHelper.closeConnection()
            }
        }
    }

    fun eliminarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val connection = dbHelper.getConnection()
            val query = """
                    DELETE FROM [dbo].[USUARIOS] 
                    WHERE id_usuarios = ?
                """.trimIndent()


            val preparedStatement = connection?.prepareStatement(query)

            try {
                preparedStatement?.setInt(1, usuario.id)

                var rowsAffected = 0
                rowsAffected = preparedStatement?.executeUpdate() ?: 0

                if (rowsAffected > 0) {
                    cargarUsuarios() // Recargar la lista
                    _uiEventDelete.send(UIEventDelete.UserDeletedSuccess)
                } else {
                    _uiEventDelete.send(UIEventDelete.Error("No se pudo eliminar el usuario"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEventDelete.send(UIEventDelete.Error(e.message ?: "Error desconocido"))
            } finally {
                preparedStatement?.close()
                dbHelper.closeConnection()
            }
        }
    }

    fun editarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val connection = dbHelper.getConnection()

            val query = """
                UPDATE Usuarios 
                SET nombres = ?, 
                    apellidos = ?, 
                    email = ?, 
                    ci_ruc = ? 
                WHERE id_usuarios = ?
                """.trimIndent()

            val preparedStatement = connection?.prepareStatement(query)

            try {
                preparedStatement?.setString(1, usuario.nombres)
                preparedStatement?.setString(2, usuario.apellidos)
                preparedStatement?.setString(3, usuario.email)
                preparedStatement?.setString(4, usuario.ruc)
                preparedStatement?.setInt(5, usuario.id)
                var rowsAffected = 0
                rowsAffected = preparedStatement?.executeUpdate() ?: 0

                if (rowsAffected > 0) {
                    cargarUsuarios() // Recargar la lista
                    _uiEventUpdate.send(UIEventUpdate.UserUpdatedSuccess)
                } else {
                    _uiEventUpdate.send(UIEventUpdate.Error("No se pudo actualizar información de usuario"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEventUpdate.send(UIEventUpdate.Error(e.message ?: "Error desconocido"))
            } finally {
                preparedStatement?.close()
                dbHelper.closeConnection()
            }
        }
    }

    fun cambiarContrasenaUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val connection = dbHelper.getConnection()
            val query = """
                UPDATE Usuarios 
                SET password = ? 
                WHERE id_usuarios = ?
                """.trimIndent()

            val contrasenaHash = hashearString(usuario.contrasena)

            val preparedStatement = connection?.prepareStatement(query)

            try {

                preparedStatement?.setString(1, contrasenaHash)
                preparedStatement?.setInt(2, usuario.id)

                var rowsAffected = 0
                rowsAffected = preparedStatement?.executeUpdate() ?: 0

                if (rowsAffected > 0) {
                    cargarUsuarios() // Recargar la lista
                    _uiEventUpdatePass.send(UIEventUpdatePass.UserUpdatedPassSuccess)
                } else {
                    _uiEventUpdatePass.send(UIEventUpdatePass.Error("No se pudo actualizar información de usuario"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEventUpdatePass.send(UIEventUpdatePass.Error(e.message ?: "Error desconocido"))
            } finally {
                preparedStatement?.close()
                dbHelper.closeConnection()
            }
        }
    }

    fun getUsuarioById(id: Int): Usuario? {
        return usuarios.value.firstOrNull { it.id == id }
    }

}

fun sanearString(input: String): String {
    return input.trim()
        .replace("'", "")
        .replace("\"", "")
        .replace(";", "")
        .replace("--", "")
        .replace("/*", "")
        .replace("*/", "")
}

fun hashearString(input: String): String {
    // El hash es utilizando la libreria java.security.MessageDigest
    // con las siguientes caracteristicas
    val data = input.toByteArray()
    val digestInstance = MessageDigest.getInstance("MD5")
    val hashValue = digestInstance.digest(data)
    return hashValue.joinToString("") { "%02x".format(it) }
}