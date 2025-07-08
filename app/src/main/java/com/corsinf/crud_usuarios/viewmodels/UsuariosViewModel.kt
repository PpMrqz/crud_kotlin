package com.corsinf.crud_usuarios.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corsinf.crud_usuarios.data.DatabaseHelper
import com.corsinf.crud_usuarios.data.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.sql.ResultSet

class UsuariosViewModel(private val context: Context) : ViewModel() {
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
            try {
                val statement = connection?.createStatement()
                val resultSet = statement?.executeQuery(
                    """
                    SELECT [nombres], [apellidos], [id_usuarios], [email], [ci_ruc]
                    FROM [dbo].[USUARIOS]
                    """
                )
                println("EJECUTANDO SQL")
                while (resultSet?.next() == true) {
                    println(resultSet)
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
                dbHelper.closeConnection()
                _isLoading.value = false
            }
        }
    }

}