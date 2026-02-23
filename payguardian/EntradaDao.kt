package com.example.payguardian

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntradaDao {

    // 1. Comando para salvar uma nova entrada
    @Insert
    suspend fun inserir(entrada: Entrada)

    // 2. NOVO: Comando para ATUALIZAR um registro existente
    // O Room usa o 'id' da Entrada para saber qual linha deve ser alterada no banco
    @Update
    suspend fun atualizar(entrada: Entrada)

    // 3. NOVO: Comando para DELETAR um registro
    @Delete
    suspend fun deletar(entrada: Entrada)

    // 4. Comando para listar tudo (do mais novo para o mais antigo)
    @Query("SELECT * FROM tabela_entradas ORDER BY data DESC")
    fun listarTodas(): Flow<List<Entrada>>

    // 5. A lógica da sua soma (dia, mês, ano)
    @Query("SELECT SUM(valor) FROM tabela_entradas WHERE data BETWEEN :inicio AND :fim")
    fun somarNoPeriodo(inicio: Long, fim: Long): Flow<Double?>
}