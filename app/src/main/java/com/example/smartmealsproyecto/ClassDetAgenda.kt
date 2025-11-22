package com.example.smartmealsproyecto

import java.sql.Time

data class ClassDetAgenda(
    val idAg: Int,
    val id: Int,
    val tipoCom: String,
    val nombre: String,
    val hora: String,
    val notas: String
)