package com.example.payguardian

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Definimos quais entidades o banco possui e a versão
@Database(entities = [Entrada::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // 2. Conectamos o nosso DAO à Database
    abstract fun entradaDao(): EntradaDao

    // 3. O Singleton (Garante que só exista uma "Placa-Mãe" ligada por vez)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "relatorio_database" // Nome do arquivo do banco no celular
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}