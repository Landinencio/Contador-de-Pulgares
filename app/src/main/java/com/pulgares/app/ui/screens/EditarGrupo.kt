package com.pulgares.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.data.EstadoGrupo
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Dinero
import com.pulgares.app.frases.Frases
import com.pulgares.app.ui.components.BotonPegatina
import com.pulgares.app.ui.components.BotonRedondo
import com.pulgares.app.ui.components.Chapa
import com.pulgares.app.ui.components.DialogoConfirmar
import com.pulgares.app.ui.components.Pegatina
import com.pulgares.app.ui.theme.Paleta

/** Los mismos iconos que ofrece la pantalla de crear grupo. */
private val emojis = listOf(
    "🍻", "✈️", "🏠", "🎉", "🏖️", "⛷️", "🍕", "🎣", "🚐", "🎸", "⚽", "🎰", "🧗", "🎬", "👥"
)

/**
 * Retocar un grupo ya creado: nombre, icono, quién está dentro y el monigote de
 * cada uno. Antes solo se podía decidir todo esto al crearlo, y si te olvidabas
 * de un colega no había vuelta atrás.
 */
@Composable
fun EditarGrupoScreen(
    estado: EstadoGrupo,
    onGuardarNombre: (String, String) -> Unit,
    onAnadirColega: (String) -> Unit,
    onQuitarColega: (Colega) -> Unit,
    onEditarAvatarDe: (Colega) -> Unit,
    onRenombrarColega: (Colega, String) -> Unit,
    onBorrarGrupo: () -> Unit,
    onVolver: () -> Unit
) {
    val grupo = estado.grupo
    var nombre by remember(grupo.id) { mutableStateOf(grupo.nombre) }
    var emoji by remember(grupo.id) { mutableStateOf(grupo.emoji) }
    var nuevoColega by remember { mutableStateOf("") }
    var aQuitar by remember { mutableStateOf<Colega?>(null) }
    var borrandoGrupo by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonRedondo(contenido = "‹", onClick = onVolver, descripcion = "Volver al grupo")
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Ajustes del grupo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // ---- nombre e icono ----
        item {
            Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 4.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del grupo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { candidato ->
                            val elegido = candidato == emoji
                            Pegatina(
                                color = if (elegido) Paleta.MostazaSuave else MaterialTheme.colorScheme.surface,
                                borde = if (elegido) Paleta.RosaChicle else Paleta.Tinta,
                                grosorBorde = if (elegido) 3.5.dp else 2.dp,
                                radio = 14.dp,
                                sombra = if (elegido) 3.dp else 2.dp,
                                onClick = { emoji = candidato }
                            ) {
                                Text(
                                    text = candidato,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    BotonPegatina(
                        texto = "Guardar nombre e icono",
                        emoji = "✏️",
                        habilitado = nombre.isNotBlank() &&
                            (nombre != grupo.nombre || emoji != grupo.emoji),
                        onClick = { onGuardarNombre(nombre.trim(), emoji) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ---- la peña ----
        item {
            Text(
                text = "La peña (${grupo.colegas.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "Toca un monigote para cambiárselo. Con la chincheta se queda como está.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(grupo.colegas, key = { it.id }) { colega ->
            FilaColega(
                colega = colega,
                saldo = estado.saldoDe(colega.id),
                cuantosGastos = estado.gastos.count { it.pagadorId == colega.id },
                puedeQuitarse = !colega.soyYo && grupo.colegas.size > 2,
                onEditarAvatar = { onEditarAvatarDe(colega) },
                onRenombrar = { onRenombrarColega(colega, it) },
                onQuitar = { aQuitar = colega }
            )
        }

        // ---- añadir a alguien ----
        item {
            Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 3.dp) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevoColega,
                        onValueChange = { nuevoColega = it },
                        label = { Text("¿Se ha apuntado alguien más?") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(10.dp))
                    BotonRedondo(
                        contenido = "➕",
                        descripcion = "Añadir al grupo",
                        color = Paleta.VerdePaz,
                        onClick = {
                            if (nuevoColega.isNotBlank()) {
                                onAnadirColega(nuevoColega.trim())
                                nuevoColega = ""
                            }
                        }
                    )
                }
            }
            Text(
                text = "Los gastos ya apuntados no cambian: el nuevo entra solo en los siguientes.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }

        // ---- borrar el grupo, al final y con aviso ----
        item {
            Spacer(Modifier.height(18.dp))
            Pegatina(
                modifier = Modifier.fillMaxWidth(),
                color = Paleta.RojoDeudaSuave,
                sombra = 3.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cerrar el grupo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Paleta.Tinta
                    )
                    Text(
                        text = if (estado.enPaz) {
                            "Está todo a cero, así que no se pierde ninguna cuenta pendiente."
                        } else {
                            "Ojo: aún hay cuentas sin saldar."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Paleta.TintaSuave
                    )
                    Spacer(Modifier.height(12.dp))
                    BotonPegatina(
                        texto = "Borrar el grupo",
                        emoji = "🗑",
                        color = Paleta.RojoDeuda,
                        onClick = { borrandoGrupo = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (borrandoGrupo) {
        DialogoConfirmar(
            titulo = "¿Borrar «${grupo.nombre}»?",
            mensaje = buildString {
                append("Se van ")
                append(
                    when (estado.gastos.size) {
                        0 -> "el grupo entero"
                        1 -> "el grupo y su único gasto"
                        else -> "el grupo y sus ${estado.gastos.size} gastos"
                    }
                )
                append(", y no hay manera de recuperarlo. ")
                if (!estado.enPaz) {
                    append("Encima quedan cuentas sin saldar: ")
                    append(Dinero.formatea(estado.saldos.filter { it.esDeudor }.sumOf { -it.neto }))
                    append(" en total.")
                } else {
                    append("Al menos está todo pagado.")
                }
            },
            textoConfirmar = "Borrar",
            onConfirmar = {
                borrandoGrupo = false
                onBorrarGrupo()
            },
            onCancelar = { borrandoGrupo = false }
        )
    }

    // ---- aviso antes de echar a alguien ----
    val candidato = aQuitar
    if (candidato != null) {
        val apareceEn = estado.gastos.count {
            it.pagadorId == candidato.id || it.deudas().containsKey(candidato.id)
        }
        DialogoConfirmar(
            titulo = "¿Fuera ${candidato.nombre}?",
            mensaje = if (apareceEn > 0) {
                "Sale de la lista, pero sigue apareciendo en $apareceEn " +
                    (if (apareceEn == 1) "gasto ya apuntado" else "gastos ya apuntados") +
                    ", así que las cuentas de esos gastos no cambian. Si tiene saldo pendiente, " +
                    "seguirá saliendo en el plan de pagos."
            } else {
                "No aparece en ningún gasto, así que no se pierde nada."
            },
            textoConfirmar = "Fuera",
            emoji = "👋",
            onConfirmar = {
                onQuitarColega(candidato)
                aQuitar = null
            },
            onCancelar = { aQuitar = null }
        )
    }
}

@Composable
private fun FilaColega(
    colega: Colega,
    saldo: Long,
    cuantosGastos: Int,
    puedeQuitarse: Boolean,
    onEditarAvatar: () -> Unit,
    onRenombrar: (String) -> Unit,
    onQuitar: () -> Unit
) {
    var editando by remember { mutableStateOf(false) }
    var nombre by remember(colega.id) { mutableStateOf(colega.nombre) }

    Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 3.dp) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pegatina(
                    radio = 50.dp,
                    sombra = 2.dp,
                    color = Paleta.RosaMonigote,
                    onClick = onEditarAvatar
                ) {
                    AvatarMonigote(
                        monigote = avatarDe(colega),
                        tamano = 50,
                        conFondo = false,
                        descripcion = "Cambiar el monigote de ${colega.nombre}"
                    )
                }
                Spacer(Modifier.width(12.dp))
                if (editando) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = colega.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (colega.soyYo) {
                                Spacer(Modifier.width(6.dp))
                                Chapa(texto = "tú", color = Paleta.MostazaSuave)
                            }
                        }
                        Text(
                            text = detalleColega(saldo, cuantosGastos),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (editando) {
                    BotonRedondo(
                        contenido = "✓",
                        descripcion = "Guardar el nombre",
                        color = Paleta.VerdePaz,
                        onClick = {
                            if (nombre.isNotBlank()) onRenombrar(nombre.trim())
                            editando = false
                        }
                    )
                } else {
                    BotonRedondo(
                        contenido = "✏️",
                        descripcion = "Cambiar el nombre de ${colega.nombre}",
                        onClick = { editando = true },
                        sombra = 2.dp
                    )
                    if (puedeQuitarse) {
                        Spacer(Modifier.width(6.dp))
                        BotonRedondo(
                            contenido = "✕",
                            descripcion = "Sacar a ${colega.nombre} del grupo",
                            color = Paleta.RojoDeudaSuave,
                            onClick = onQuitar,
                            sombra = 2.dp
                        )
                    }
                }
            }
        }
    }
}

private fun detalleColega(saldo: Long, cuantosGastos: Int): String {
    val estado = when {
        saldo > 0 -> "le deben ${Dinero.formatea(saldo)}"
        saldo < 0 -> "debe ${Dinero.formatea(-saldo)}"
        else -> "en paz"
    }
    val rango = Frases.rangoPagador(if (saldo > 0) saldo else 0L, cuantosGastos)
    return "$estado · $rango"
}
