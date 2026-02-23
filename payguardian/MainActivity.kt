package com.example.payguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payguardian.ui.theme.PayguardianTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialização do Banco de Dados Room através do Singleton na AppDatabase
        val database = AppDatabase.getDatabase(this)
        val dao = database.entradaDao()

        // Inicialização do ViewModel que gerencia a lógica de dados
        val viewModel = EntradaViewModel(dao)

        enableEdgeToEdge()

        setContent {
            PayguardianTheme {
                // Estado que controla a exibição da tela de abertura
                var mostrarSplash by remember { mutableStateOf(true) }

                if (mostrarSplash) {
                    // Exibe a tela verde com a marca WSistemas
                    SplashScreen(onFinished = { mostrarSplash = false })
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                        // Observa a lista de entradas do banco de dados em tempo real
                        val entradas by viewModel.todasAsEntradas.collectAsState(initial = emptyList())

                        // Estado para controlar se estamos editando um item ou criando um novo
                        var entradaParaEditar by remember { mutableStateOf<Entrada?>(null) }

                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            // 1. FORMULÁRIO: Agora preparado para receber a data manual da UI
                            TelaCadastroEntrada(
                                entradaExistente = entradaParaEditar,
                                onSalvarClick = { valor, banco, nome, dataManual ->
                                    if (entradaParaEditar == null) {
                                        // Chama a nova função de salvar com 4 parâmetros (valor, banco, nome, data)
                                        viewModel.salvarEntrada(valor, banco, nome, dataManual)
                                    } else {
                                        // No modo edição, permite que a data também seja atualizada manualmente
                                        val entradaAtualizada = entradaParaEditar!!.copy(
                                            valor = valor,
                                            banco = banco,
                                            nomePessoa = nome,
                                            data = dataManual
                                        )
                                        viewModel.atualizarEntrada(entradaAtualizada)
                                        entradaParaEditar = null // Reseta o estado para voltar ao modo de inserção
                                    }
                                },
                                onCancelarEdicao = { entradaParaEditar = null }
                            )

                            // 2. DASHBOARD: Exibe os cards de ganhos baseados na lista atual
                            DashboardGanhos(lista = entradas)

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp
                            )

                            // 3. LISTA: Mostra o histórico e fornece as ações de clique e deleção
                            ListaEntradas(
                                entradas = entradas,
                                onItemClick = { item -> entradaParaEditar = item },
                                onDeleteClick = { item -> viewModel.deletarEntrada(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente Visual da Splash Screen
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // Aguarda 2 segundos antes de liberar o acesso ao app
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PayGuardian",
            color = Color(0xFFC8E6C9),
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WSistemas",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "v0.1",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}