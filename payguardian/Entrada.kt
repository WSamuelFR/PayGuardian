package com.example.payguardian

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabela_entradas")
data class Entrada(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val data: Long,
    val valor: Double,
    val banco: String,
    val nomePessoa: String
)