package com.corsinf.crud_usuarios.data

import kotlin.text.Regex

object AppRegex {
    // Objetos regex. Para revisar presencia en un string
    val tieneLetrasMayus = Regex("[A-Z]") // Al menos una mayuscula
    val tieneLetrasMinus = Regex("[a-z]") // Al menos una minuscula
    val tieneNumeros = Regex("\\d") // Al menos un número
    val tieneEspChar = Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*") // Al menos un caracter especial

    // String con regex. Para match completo del string
    const val EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"
    const val RUC = "^[0-9]{10}\$"
    const val CI = "^[0-9]{13}\$"

    // Para ver si un string se limita solo a un conjunto de caracteres
    const val EMAIL_CHARS = "^[A-Za-z0-9+_.-@]+$"
    const val NUM_CHARS = "^\\d*\$"

    //

}