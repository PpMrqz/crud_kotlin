package com.corsinf.crud_usuarios.data


import android.content.Context
import com.corsinf.crud_usuarios.BuildConfig
import java.sql.Connection
import java.sql.DriverManager
import java.io.File
import java.io.FileInputStream
import java.util.*

//import com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11

class DatabaseHelper(private val context: Context) {
    private var connection: Connection? = null

    fun getConnection(): Connection? {
        return try {

            val ip = BuildConfig.DB_IP
            val port = BuildConfig.DB_PORT
            val dbName = BuildConfig.DB_NAME
            val user = BuildConfig.DB_USER
            val password = BuildConfig.DB_PASSWORD
            val loginTimeout = 5 // Timeout para establecer conexión
            val socketTimeout = 10 // Timeout para operaciones después de conectado

            // microsoft garca usa en su jdbc oficial una llamada a apis de android
            // escondidas/prohibidas para conectarse a su base de datos, lo cual genera
            // crasheo instantaneo.
            // Estas llamadas de api escondidas/prohibidas o "hiddenapi" estan estratificadas
            // por listas, dependiendo de que tan malo/prohibido sea su acceso, y no poseen
            // documentación, pues no se supone que deban ser accesadas.
            // Existen formas de forzar a android que permita el acceso, pero estos metodos de
            // dudosa procedencia no son recomendados.
            // Lo mas probable es que en mejores tiempos, microsoft uso estas apis, pero cuando
            // fueron añadidas a la lista, esto jamas se rectifico.
            // Conclusion, ahora hay que utilizar un jdbc sacado de sourceforge cuyo desarrollo
            // fue abandonado creo que 2013, y aparentemente esta es la solucion
            // recomendada por la gente en internet que encontre habian implementado conexion
            // directa a ms sql server desde android.
            // SIN EMBARGO la solucion verdadera es hacer un layer de api que llame a la base de
            // datos, y de android a api, algo como: BDD <- REST API <- Android


            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            //val url = "jdbc:sqlserver://$ip:$port;databaseName=$dbName;encrypt=true;trustServerCertificate=true"
            //connection = DriverManager.getConnection(url, user, password)
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            var url = "jdbc:jtds:sqlserver://$ip:$port;databaseName=$dbName;user=$user;password=$password;"
            url += "loginTimeout=$loginTimeout; socketTimeout=$socketTimeout"
            connection = DriverManager.getConnection(url)
            connection
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun closeConnection() {
        try {
            println("Conexion cerrada")
            connection?.close()
        } catch (e: Exception) {
            println("Fallo en cerrar la conexion")
            e.printStackTrace()
        }
    }
}
object PropertiesReader {
    fun getProperty(key: String, context: Context): String {
        val properties = Properties()
        context.assets.open("local.properties").use {
            properties.load(it)
        }
        return properties.getProperty(key)
    }
}
