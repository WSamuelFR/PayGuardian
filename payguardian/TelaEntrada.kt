package com.example.payguardian

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// --- 1. FORMULÁRIO DE CADASTRO E EDIÇÃO ---
@Composable
fun TelaCadastroEntrada(
    entradaExistente: Entrada?,
    onSalvarClick: (Double, String, String, Long) -> Unit,
    onCancelarEdicao: () -> Unit
) {
    // Estados para os textos dos campos
    var valor by remember { mutableStateOf("") }
    var banco by remember { mutableStateOf("") }
    var nomePessoa by remember { mutableStateOf("") }

    // Estados para controle de erro (Validação)
    var erroValor by remember { mutableStateOf(false) }
    var erroBanco by remember { mutableStateOf(false) }
    var erroNome by remember { mutableStateOf(false) }

    val sdfSimples = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var dataTexto by remember { mutableStateOf(sdfSimples.format(Date())) }

    // Sincroniza os campos quando um item é selecionado para editar
    LaunchedEffect(entradaExistente) {
        if (entradaExistente != null) {
            valor = entradaExistente.valor.toString()
            banco = entradaExistente.banco
            nomePessoa = entradaExistente.nomePessoa
            dataTexto = sdfSimples.format(Date(entradaExistente.data))
        } else {
            valor = ""; banco = ""; nomePessoa = ""
            dataTexto = sdfSimples.format(Date())
        }
        // Limpa avisos de erro ao carregar novos dados
        erroValor = false; erroBanco = false; erroNome = false
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (entradaExistente == null) "Nova Entrada" else "Editando Registro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (entradaExistente == null) MaterialTheme.colorScheme.primary else Color.Red
            )

            // Campo de Valor com validação de erro
            OutlinedTextField(
                value = valor,
                onValueChange = { valor = it; erroValor = false },
                label = { Text("Valor") },
                isError = erroValor,
                prefix = { Text("R$ ") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Máscara de Data Segura
            OutlinedTextField(
                value = dataTexto,
                onValueChange = { input ->
                    val cleaned = input.filter { it.isDigit() }
                    val limited = if (cleaned.length > 8) cleaned.substring(0, 8) else cleaned
                    val formatted = StringBuilder()
                    for (i in limited.indices) {
                        formatted.append(limited[i])
                        if ((i == 1 || i == 3) && i != limited.lastIndex) {
                            formatted.append("/")
                        }
                    }
                    dataTexto = formatted.toString()
                },
                label = { Text("Data (DD/MM/AAAA)") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Campo Banco com validação
            OutlinedTextField(
                value = banco,
                onValueChange = { banco = it; erroBanco = false },
                label = { Text("Banco") },
                isError = erroBanco,
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Campo Pagador com validação
            OutlinedTextField(
                value = nomePessoa,
                onValueChange = { nomePessoa = it; erroNome = false },
                label = { Text("Pagador") },
                isError = erroNome,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 1. Sanitização dos dados (remove espaços vazios nas pontas)
                        val vTrim = valor.trim()
                        val bTrim = banco.trim()
                        val nTrim = nomePessoa.trim()

                        // 2. Validação lógica: Verifica se os campos estão preenchidos
                        erroValor = vTrim.isEmpty()
                        erroBanco = bTrim.isEmpty()
                        erroNome = nTrim.isEmpty()

                        // 3. Só prossegue se todos os campos estiverem OK
                        if (!erroValor && !erroBanco && !erroNome) {
                            val valorDouble = vTrim.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (valorDouble > 0.0) {
                                try {
                                    val dataLong = sdfSimples.parse(dataTexto)?.time ?: System.currentTimeMillis()
                                    onSalvarClick(valorDouble, bTrim, nTrim, dataLong)
                                    // Limpa os campos após o sucesso
                                    valor = ""; banco = ""; nomePessoa = ""
                                } catch (e: Exception) { /* Data inválida ignorada */ }
                            } else {
                                erroValor = true // Valor zero não é permitido
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (entradaExistente == null) "Confirmar" else "Atualizar")
                }

                if (entradaExistente != null) {
                    OutlinedButton(
                        onClick = onCancelarEdicao,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

// --- 2. DASHBOARD DE GANHOS ---
@Composable
fun DashboardGanhos(lista: List<Entrada>) {
    val hoje = System.currentTimeMillis()

    val totalDia = lista.filter { isMesmoDia(it.data, hoje) }.sumOf { it.valor }
    val totalMes = lista.filter { isMesmoMes(it.data, hoje) }.sumOf { it.valor }
    val totalAno = lista.filter { isMesmoAno(it.data, hoje) }.sumOf { it.valor }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Resumo de Ganhos",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardGanho("Hoje", totalDia, Modifier.weight(1f), MaterialTheme.colorScheme.primaryContainer)
            CardGanho("Mês", totalMes, Modifier.weight(1f), MaterialTheme.colorScheme.secondaryContainer)
            CardGanho("Ano", totalAno, Modifier.weight(1f), MaterialTheme.colorScheme.tertiaryContainer)
        }
    }
}

@Composable
fun CardGanho(titulo: String, valor: Double, modifier: Modifier, cor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelSmall)
            Text(
                text = "R$ ${String.format("%.2f", valor)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// --- 3. LISTA DE HISTÓRICO ---
@Composable
fun ListaEntradas(
    entradas: List<Entrada>,
    onItemClick: (Entrada) -> Unit,
    onDeleteClick: (Entrada) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Histórico Recente",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
        )

        if (entradas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Nenhum registro encontrado.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(entradas) { entrada ->
                    ItemEntrada(
                        entrada = entrada,
                        onClick = onItemClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
fun ItemEntrada(
    entrada: Entrada,
    onClick: (Entrada) -> Unit,
    onDeleteClick: (Entrada) -> Unit
) {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dataFormatada = formatter.format(Date(entrada.data))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick(entrada) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entrada.nomePessoa, fontWeight = FontWeight.SemiBold)
                Text(text = "Banco: ${entrada.banco}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = dataFormatada, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Text(
                text = "R$ ${String.format("%.2f", entrada.valor)}",
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(onClick = { onDeleteClick(entrada) }) {
                Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = Color.LightGray)
            }
        }
    }
}

// --- 4. FUNÇÕES DE UTILIDADE (DATAS) ---
fun isMesmoDia(d1: Long, d2: Long): Boolean =
    SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(d1)) ==
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(d2))

fun isMesmoMes(d1: Long, d2: Long): Boolean =
    SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date(d1)) ==
            SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date(d2))

fun isMesmoAno(d1: Long, d2: Long): Boolean =
    SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(d1)) ==
            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(d2))