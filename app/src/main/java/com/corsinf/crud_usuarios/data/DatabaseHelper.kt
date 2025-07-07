package com.corsinf.crud_usuarios.data

import com.microsoft.sqlserver.jdbc.SQLServerDriver
import android.content.Context
import java.sql.Connection
import java.sql.DriverManager
import java.io.File
import java.io.FileInputStream
import java.util.*

class DatabaseHelper(private val context: Context) {
    private var connection: Connection? = null

    fun getConnection(): Connection? {
        return try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            val ip = PropertiesReader.getProperty("db.ip", context)
            val port = PropertiesReader.getProperty("db.port", context)
            val dbName = PropertiesReader.getProperty("db.name", context)
            val user = PropertiesReader.getProperty("db.user", context)
            val password = PropertiesReader.getProperty("db.password", context)

            val url = "jdbc:sqlserver://$ip:$port;databaseName=$dbName;encrypt=true;trustServerCertificate=true"

            connection = DriverManager.getConnection(url, user, password)
            connection
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun closeConnection() {
        try {
            connection?.close()
        } catch (e: Exception) {
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
