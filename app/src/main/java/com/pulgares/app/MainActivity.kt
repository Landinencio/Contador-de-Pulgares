package com.pulgares.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulgares.app.data.Repositorio
import com.pulgares.app.data.local.BaseDatos
import com.pulgares.app.domain.model.Dinero
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento
import com.pulgares.app.ui.PulgaresViewModel
import com.pulgares.app.ui.screens.DetalleGrupoScreen
import com.pulgares.app.ui.screens.EditorAvatarScreen
import com.pulgares.app.ui.screens.NuevoGastoScreen
import com.pulgares.app.ui.screens.NuevoGrupoScreen
import com.pulgares.app.ui.screens.PortadaScreen
import com.pulgares.app.ui.theme.TemaPulgares
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = Repositorio(BaseDatos.obten(applicationContext))
        setContent {
            TemaPulgares {
                AppPulgares(repo)
            }
        }
    }
}

/** Las pantallas de la app. Son cuatro; no hace falta traerse una libreria. */
private sealed interface Pantalla {
    data object Portada : Pantalla
    data object NuevoGrupo : Pantalla
    data class Grupo(val grupoId: String) : Pantalla
    data class Gasto(val grupoId: String, val gastoId: String?) : Pantalla
    data object MiAvatar : Pantalla
}

@Composable
fun AppPulgares(repo: Repositorio) {
    val vm: PulgaresViewModel = viewModel(factory = PulgaresViewModel.Fabrica(repo))
    val grupos by vm.grupos.collectAsStateWithLifecycle()
    val estadoGrupo by vm.estadoGrupo.collectAsStateWithLifecycle()
    val miAvatar by vm.miAvatar.collectAsStateWithLifecycle()

    var pantalla by remember { mutableStateOf<Pantalla>(Pantalla.Portada) }
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()

    fun avisa(texto: String) {
        alcance.launch { avisos.showSnackbar(texto) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = MaterialTheme.colorScheme.background
    ) { margenes ->
        Box(modifier = Modifier.fillMaxSize().padding(margenes)) {
            when (val actual = pantalla) {
                Pantalla.Portada -> PortadaScreen(
                    grupos = grupos,
                    miAvatar = miAvatar,
                    onAbrirGrupo = { id ->
                        vm.abreGrupo(id)
                        pantalla = Pantalla.Grupo(id)
                    },
                    onNuevoGrupo = { pantalla = Pantalla.NuevoGrupo },
                    onEditarAvatar = { pantalla = Pantalla.MiAvatar }
                )

                Pantalla.NuevoGrupo -> NuevoGrupoScreen(
                    onCrear = { nombre, emoji, colegas, miNombre ->
                        vm.creaGrupo(nombre, emoji, colegas, miNombre) { id ->
                            pantalla = Pantalla.Grupo(id)
                        }
                    },
                    onVolver = { pantalla = Pantalla.Portada }
                )

                is Pantalla.Grupo -> {
                    val estado = estadoGrupo
                    if (estado == null || estado.grupo.id != actual.grupoId) {
                        Cargando()
                    } else {
                        DetalleGrupoScreen(
                            estado = estado,
                            onVolver = { pantalla = Pantalla.Portada },
                            onNuevoGasto = { pantalla = Pantalla.Gasto(actual.grupoId, null) },
                            onEditarGasto = { gasto ->
                                pantalla = Pantalla.Gasto(actual.grupoId, gasto.id)
                            },
                            onVotar = { gastoId, arriba ->
                                val miId = estado.grupo.yo?.id
                                if (miId != null) vm.votaGasto(gastoId, miId, arriba)
                            },
                            onPagar = { transferencia ->
                                vm.registraPago(
                                    grupoId = actual.grupoId,
                                    deQuienId = transferencia.deQuienId,
                                    aQuienId = transferencia.aQuienId,
                                    importe = transferencia.importeCentimos
                                )
                                val soyYo = transferencia.deQuienId == estado.grupo.yo?.id
                                avisa(
                                    if (soyYo) {
                                        Frases.para(
                                            Momento.PAGASTE,
                                            quien = estado.grupo.nombreDe(transferencia.deQuienId),
                                            cuanto = Dinero.formatea(transferencia.importeCentimos)
                                        )
                                    } else {
                                        Frases.para(
                                            Momento.TE_HAN_PAGADO,
                                            quien = estado.grupo.nombreDe(transferencia.deQuienId),
                                            cuanto = Dinero.formatea(transferencia.importeCentimos)
                                        )
                                    }
                                )
                            },
                            onDarToque = { colega, deuda ->
                                // De momento el toque es local: se avisa aqui. Cuando
                                // se active la sincronizacion, saldra como notificacion
                                // en el movil del moroso.
                                avisa(
                                    Frases.para(
                                        Momento.DEBES,
                                        quien = colega.nombre,
                                        cuanto = Dinero.formatea(deuda)
                                    )
                                )
                            },
                            onBorrarGrupo = {
                                vm.borraGrupo(actual.grupoId)
                                pantalla = Pantalla.Portada
                            }
                        )
                    }
                }

                is Pantalla.Gasto -> {
                    val estado = estadoGrupo
                    if (estado == null) {
                        Cargando()
                    } else {
                        val existente: Gasto? = actual.gastoId?.let { id ->
                            estado.gastos.firstOrNull { it.id == id }
                        }
                        NuevoGastoScreen(
                            colegas = estado.grupo.colegas,
                            gastoExistente = existente,
                            onGuardar = { concepto, importe, pagadorId, categoria, reparto, nota ->
                                vm.apuntaGasto(
                                    grupoId = actual.grupoId,
                                    concepto = concepto,
                                    importeCentimos = importe,
                                    pagadorId = pagadorId,
                                    categoria = categoria,
                                    reparto = reparto,
                                    nota = nota,
                                    gastoExistenteId = existente?.id
                                )
                                pantalla = Pantalla.Grupo(actual.grupoId)
                                avisa(
                                    Frases.para(
                                        Momento.GASTO_NUEVO,
                                        quien = estado.grupo.nombreDe(pagadorId),
                                        cuanto = Dinero.formatea(importe),
                                        que = concepto
                                    )
                                )
                            },
                            onBorrar = existente?.let { gasto ->
                                {
                                    vm.borraGasto(gasto.id)
                                    pantalla = Pantalla.Grupo(actual.grupoId)
                                }
                            },
                            onVolver = { pantalla = Pantalla.Grupo(actual.grupoId) }
                        )
                    }
                }

                Pantalla.MiAvatar -> EditorAvatarScreen(
                    inicial = miAvatar,
                    onGuardar = { monigote ->
                        vm.guardaMiAvatar(monigote)
                        pantalla = Pantalla.Portada
                        avisa("Monigote actualizado. Menudo careto.")
                    },
                    onVolver = { pantalla = Pantalla.Portada }
                )
            }
        }
    }
}

@Composable
private fun Cargando() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Contando pulgares…",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(24.dp)
        )
    }
}
