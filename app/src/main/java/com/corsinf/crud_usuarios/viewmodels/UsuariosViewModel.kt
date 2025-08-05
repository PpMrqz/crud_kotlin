package com.corsinf.crud_usuarios.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corsinf.crud_usuarios.data.Busqueda
import com.corsinf.crud_usuarios.data.DatabaseHelper
import com.corsinf.crud_usuarios.data.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.sql.SQLException

class UsuariosViewModel(private val context: Context) : ViewModel() {
    private val _usuarios = MutableStateFlow<MutableList<Usuario>>(mutableListOf())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error de conexion, de momento utilizado solamente para manejar la carga de usuarios
    private val _errorConexion = MutableStateFlow<String?>(null)
    val errorConexion: StateFlow<String?> = _errorConexion

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage

    // Variables para guardar la ultima busqueda
    val pagina = mutableStateOf(1)
    val usuariosPorPagina = mutableStateOf(20)
    val textoBusqueda = mutableStateOf("")
    val campoBusqueda = mutableStateOf("nombre")


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
    // Eventos de navegación por interfaz de usuario
    /*val channelEventNav = Channel<UIEventNav>()
    val uiEventNav = channelEventNav.receiveAsFlow()
    sealed class UIEventNav {
        data class Error(val message: String) : UIEventNav()
    }*/

    // El viewmodel cargara usuarios al iniciar para tenerlos disponibles desde el inicio
    init {
        buscarUsuariosConReintento(1)
    }

    private suspend fun cargarUsuarios() {
        return withContext(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val connection = dbHelper.getConnection()
            if (connection == null) {
                throw SQLException("Falló la conexión a la BD")
            }
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
                resultSet?.close()
                _usuarios.value = usuariosList
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                statement?.close()
                dbHelper.closeConnection()
            }
        }
    }

    fun repetirCargaUsuarios() {
        cargarUsuariosConReintento()
    }


    fun cargarUsuariosConReintento(maxReintentos: Int = 3, delay: Long = 2000) {
        viewModelScope.launch {
            var reintentos = 0
            var exito = false

            while (reintentos < maxReintentos && !exito) {
                try {
                    _isLoading.value = true
                    _errorConexion.value = null

                    val resultado = withContext(Dispatchers.IO) {
                        cargarUsuarios()
                    }

                    exito = true
                } catch (e: Exception) {
                    reintentos++
                    _errorConexion.value = "Error al cargar usuarios (intento $reintentos/$maxReintentos)"
                    println(_errorConexion.value)
                    if (reintentos < maxReintentos) {
                        delay(delay) // Espera antes del próximo intento
                    }
                }
            }
            _isLoading.value = false

            if (!exito) {
                _errorConexion.value = "No se pudo cargar los usuarios, revise su conexión a internet e intente de vuelta."
            }
        }
    }

    private suspend fun buscarUsuarios() {
        return withContext(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val connection = dbHelper.getConnection()
            if (connection == null) {
                throw SQLException("Falló la conexión a la BD")
            }

            val usuariosList = _usuarios.value
            val offset = (pagina.value - 1) * usuariosPorPagina.value

            val queryBase = """
            SELECT [nombres], [apellidos], [id_usuarios], [email], [ci_ruc]
            FROM [dbo].[USUARIOS]
            """



            val whereClause = if (textoBusqueda.value.isBlank()) {
                ""
            } else if (campoBusqueda.value == Busqueda.NOMBRES_APELLIDOS){
                "WHERE [nombres] LIKE ? OR [apellidos] LIKE ?"
            } else {
                "WHERE [${campoBusqueda.value}] LIKE ?"
            }

            val query = """
            $queryBase
            $whereClause
            ORDER BY [id_usuarios]
            OFFSET $offset ROWS
            FETCH NEXT ${usuariosPorPagina.value} ROWS ONLY
            """
            val statement = connection.prepareStatement(query)

            try {
                if (textoBusqueda.value.isNotBlank()) {
                    if (campoBusqueda.value == Busqueda.NOMBRES_APELLIDOS) {
                        statement.setString(1, "%${textoBusqueda}%")  // Para [nombres]
                        statement.setString(2, "%${textoBusqueda}%")  // Para [apellidos]
                    }
                    else {
                        statement.setString(1, "%${textoBusqueda}%")
                    }
                }
                println(statement)
                println(textoBusqueda)

                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
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
                resultSet.close()
                _usuarios.value = usuariosList
                _isLastPage.value = _usuarios.value.size % 20 != 0
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                statement.close()
                dbHelper.closeConnection()
            }
        }
    }

    fun buscarUsuariosConReintento(
        maxReintentos: Int = 3,
        delay: Long = 2000
    ) {
        viewModelScope.launch {
            var reintentos = 0
            var exito = false

            while (reintentos < maxReintentos && !exito) {
                try {
                    _isLoading.value = true
                    _errorConexion.value = null

                    val resultado = withContext(Dispatchers.IO) {
                        buscarUsuarios()
                    }

                    exito = true
                } catch (e: Exception) {
                    reintentos++
                    _errorConexion.value = "Error al buscar usuarios (intento $reintentos/$maxReintentos)"
                    println(_errorConexion.value)
                    if (reintentos < maxReintentos) {
                        delay(delay) // Espera antes del próximo intento
                    }
                }
            }
            _isLoading.value = false

            if (!exito) {
                _errorConexion.value = "No se pudo buscar los usuarios, revise su conexión a internet e intente de vuelta."
            }
        }
    }
    fun limpiarBusquedaAnterior(){
        _usuarios.value.clear()
    }

    fun repetirBusqueda() {
        buscarUsuariosConReintento()
    }

    fun agregarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            val existe = verificarEmailExistSuspend(usuario.email)
            if (existe) {
                _uiEventAdd.send(UIEventAdd.Error("El email ingresado ya existe"))
                _isLoading.value = false
                return@launch
            }

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
                    repetirBusqueda() // Recargar la lista
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
                _isLoading.value = false
            }
        }
    }

    fun eliminarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
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
                    repetirBusqueda() // Recargar la lista
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
                _isLoading.value = false
            }
        }
    }

    fun editarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
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
                    repetirBusqueda() // Recargar la lista
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
                _isLoading.value = false
            }
        }
    }

    fun cambiarContrasenaUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
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
                    repetirBusqueda() // Recargar la lista
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
                _isLoading.value = false
            }
        }
    }

    fun getUsuarioById(id: Int): Usuario? {
        return usuarios.value.firstOrNull { it.id == id }
    }

    suspend fun verificarEmailExistSuspend(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val connection = dbHelper.getConnection()
            var existe = false

            val query = """
                SELECT COUNT(*) as count 
                FROM [dbo].[USUARIOS] 
                WHERE [email] = ?
                """
            val preparedStatement = connection?.prepareStatement(query)

            try {
                preparedStatement?.setString(1, email)

                val resultSet = preparedStatement?.executeQuery()
                if (resultSet?.next() == true) {
                    existe = resultSet.getInt("count") > 0
                }
            } finally {
                connection?.close()
                dbHelper.closeConnection()
            }

            return@withContext existe
        }
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