package com.example.payguardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

class EntradaViewModel(private val dao: EntradaDao) : ViewModel() {

    /**
     * 1. Função para SALVAR
     * Alterada para receber a [dataManual] vinda da interface.
     * @param valor O valor monetário da entrada.
     * @param banco O nome do banco.
     * @param nome O nome do pagador.
     * @param dataManual O timestamp (Long) da data que você digitou manualmente.
     */
    fun salvarEntrada(valor: Double, banco: String, nome: String, dataManual: Long) {
        viewModelScope.launch {
            val novaEntrada = Entrada(
                valor = valor,
                banco = banco,
                nomePessoa = nome,
                data = dataManual // Agora utiliza a data escolhida por você
            )
            dao.inserir(novaEntrada) // Envia para o banco de dados via DAO
        }
    }

    /**
     * 2. Função para ATUALIZAR
     * Utilizada no modo de edição para sobrescrever um registro existente.
     */
    fun atualizarEntrada(entrada: Entrada) {
        viewModelScope.launch {
            dao.atualizar(entrada) // O Room utiliza o ID para encontrar e atualizar
        }
    }

    /**
     * 3. Função para DELETAR
     * Remove permanentemente um registro do banco de dados.
     */
    fun deletarEntrada(entrada: Entrada) {
        viewModelScope.launch {
            dao.deletar(entrada) // O Room utiliza o ID para excluir a linha correta
        }
    }

    /**
     * 4. Lista de todas as entradas
     * Um fluxo (Flow) que observa o banco e atualiza a interface automaticamente.
     */
    val todasAsEntradas: Flow<List<Entrada>> = dao.listarTodas()

    /**
     * 5. Funções de Soma
     * Utilizadas para calcular os totais exibidos no Dashboard.
     */
    fun getSomaDia(): Flow<Double?> {
        val inicio = getInicioDoDia()
        val fim = System.currentTimeMillis()
        return dao.somarNoPeriodo(inicio, fim)
    }

    /**
     * Lógica auxiliar para encontrar o primeiro milissegundo do dia atual.
     * Essencial para que a soma do dia não pegue valores de ontem.
     */
    private fun getInicioDoDia(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}