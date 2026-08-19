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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.data.EstadoGrupo
import com.pulgares.app.data.red.Sincronizador
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Dinero
import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento
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
    /** La votación del nombre en marcha, si hay alguna. */
    votacion: Sincronizador.VotacionNombre? = null,
    /** ¿Me queda mi cambio de nombre gratis? Si no, el segundo va a las urnas. */
    meQuedaCambioGratis: Boolean = true,
    onVotarNombre: (Boolean) -> Unit = {},
    onAnadirColega: (String) -> Unit,
    onQuitarColega: (Colega) -> Unit,
    onEditarAvatarDe: (Colega) -> Unit,
    onRenombrarColega: (Colega, String) -> Unit,
    /** Se avisa cuando alguien intenta rebautizar a quien no le debe nada. */
    onRenombrarBloqueado: (Colega) -> Unit = {},
    onReadmitirColega: (Colega) -> Unit,
    onBorrarGrupo: () -> Unit,
    onVolver: () -> Unit,
    /** El bloque de compartir el grupo, que lo monta quien conoce la nube. */
    bloqueCompartir: (@Composable () -> Unit)? = null
) {
    val grupo = estado.grupo
    var nombre by remember(grupo.id) { mutableStateOf(grupo.nombre) }
    var emoji by remember(grupo.id) { mutableStateOf(grupo.emoji) }
    var nuevoColega by rememberSaveable { mutableStateOf("") }
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
                    Spacer(Modifier.height(10.dp))
                    // El nombre del grupo es de todos: el primer cambio de cada
                    // uno es gratis y el segundo se somete a votación. Cambiar
                    // solo el icono nunca pasa por las urnas.
                    val soloElIcono = nombre.trim() == grupo.nombre
                    val aLasUrnas = grupo.compartido && !meQuedaCambioGratis && !soloElIcono
                    Text(
                        text = when {
                            !grupo.compartido -> "Grupo solo tuyo: aquí mandas tú."
                            soloElIcono -> "El icono se cambia sin preguntar a nadie."
                            meQuedaCambioGratis ->
                                "Te queda tu cambio de nombre gratis. El siguiente, a votación."
                            else ->
                                "Ya gastaste tu cambio gratis: esto se somete al voto del grupo."
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    BotonPegatina(
                        texto = if (aLasUrnas) "Proponer nombre al grupo" else "Guardar nombre e icono",
                        emoji = if (aLasUrnas) "🗳️" else "✏️",
                        habilitado = nombre.isNotBlank() &&
                            (nombre != grupo.nombre || emoji != grupo.emoji) &&
                            votacion == null,
                        onClick = { onGuardarNombre(nombre.trim(), emoji) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ---- la votación del nombre ----
        if (votacion != null) {
            item {
                Pegatina(
                    modifier = Modifier.fillMaxWidth(),
                    color = Paleta.MostazaSuave,
                    sombra = 4.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Chapa(texto = "🗳️ Votación en marcha", color = Paleta.RosaChicle, colorTexto = Paleta.Papel)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (votacion.esMia) {
                                "Has propuesto «${votacion.emoji} ${votacion.nombre}». " +
                                    "Ahora que decidan los demás."
                            } else {
                                Frases.para(
                                    Momento.NOMBRE_A_VOTACION,
                                    quien = votacion.quien,
                                    que = votacion.nombre,
                                    semilla = votacion.creadoMillis
                                )
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Paleta.Tinta
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "A favor ${votacion.aFavor} · en contra ${votacion.enContra} " +
                                "· votan ${votacion.votantes}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Paleta.TintaSuave
                        )
                        if (!votacion.esMia && !votacion.heVotado) {
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BotonPegatina(
                                    texto = "Me parece bien",
                                    emoji = "👍",
                                    color = Paleta.VerdePaz,
                                    onClick = { onVotarNombre(true) },
                                    modifier = Modifier.weight(1f)
                                )
                                BotonPegatina(
                                    texto = "Ni de coña",
                                    emoji = "👎",
                                    color = Paleta.RojoDeuda,
                                    onClick = { onVotarNombre(false) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else if (votacion.heVotado) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Ya has votado. Ahora toca esperar a los demás.",
                                style = MaterialTheme.typography.labelLarge,
                                color = Paleta.Tinta
                            )
                        }
                    }
                }
            }
        }

        // ---- compartir ----
        if (bloqueCompartir != null) {
            item { bloqueCompartir() }
        }

        // ---- la peña ----
        item {
            Text(
                text = "La peña (${grupo.activos.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "Toca un monigote para cambiárselo. El nombre, solo el tuyo " +
                    "y el de quien te deba dinero.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Rebautizar al resto se caparon: los nombres ajenos son suyos, no tuyos.
        // La unica excepcion es el que te debe dinero, que se lo ha ganado.
        val misDeudores = estado.plan
            .filter { it.aQuienId == grupo.yo?.id }
            .map { it.deQuienId }
            .toSet()

        items(grupo.activos, key = { it.id }) { colega ->
            FilaColega(
                colega = colega,
                puedeRenombrarse = colega.soyYo || colega.id in misDeudores,
                saldo = estado.saldoDe(colega.id),
                puestoPorEl = estado.gastos.filter { it.pagadorId == colega.id }
                    .sumOf { it.importeCentimos },
                cuantosGastos = estado.gastos.count { it.pagadorId == colega.id },
                puedeQuitarse = !colega.soyYo && grupo.activos.size > 2,
                onEditarAvatar = { onEditarAvatarDe(colega) },
                onRenombrar = { onRenombrarColega(colega, it) },
                onRenombrarBloqueado = { onRenombrarBloqueado(colega) },
                onQuitar = { aQuitar = colega }
            )
        }

        // ---- los que se fueron ----
        if (grupo.salidos.isNotEmpty()) {
            item {
                Text(
                    text = "Ya no están (${grupo.salidos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = "Siguen en los gastos de antes, con su nombre y su parte.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(grupo.salidos, key = { it.id }) { colega ->
                val saldo = estado.saldoDe(colega.id)
                Pegatina(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    sombra = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarMonigote(
                            monigote = avatarDe(colega),
                            tamano = 40,
                            conFondo = false,
                            descripcion = "Monigote de ${colega.nombre}"
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = colega.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (saldo == 0L) {
                                    "Se fue sin deudas"
                                } else if (saldo < 0) {
                                    "Se fue debiendo ${Dinero.formatea(-saldo)}"
                                } else {
                                    "Se fue y le deben ${Dinero.formatea(saldo)}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BotonRedondo(
                            contenido = "↩",
                            descripcion = "Volver a meter a ${colega.nombre} en el grupo",
                            color = Paleta.VerdePazSuave,
                            sombra = 2.dp,
                            onClick = { onReadmitirColega(colega) }
                        )
                    }
                }
            }
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
                "Deja de salir en los gastos nuevos, pero sigue en los $apareceEn " +
                    (if (apareceEn == 1) "gasto que ya tenía" else "gastos que ya tenía") +
                    ", con su nombre y su parte intactos. Si le queda saldo, sigue en el " +
                    "plan de pagos. Se le puede volver a meter cuando quieras."
            } else {
                "No aparece en ningún gasto, así que no se pierde nada. " +
                    "Se le puede volver a meter cuando quieras."
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
    puestoPorEl: Long,
    cuantosGastos: Int,
    puedeQuitarse: Boolean,
    puedeRenombrarse: Boolean,
    onEditarAvatar: () -> Unit,
    onRenombrar: (String) -> Unit,
    onRenombrarBloqueado: () -> Unit,
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
                            text = detalleColega(saldo, puestoPorEl, cuantosGastos),
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
                        contenido = if (puedeRenombrarse) "✏️" else "🔒",
                        descripcion = if (puedeRenombrarse) {
                            "Cambiar el nombre de ${colega.nombre}"
                        } else {
                            "No puedes cambiarle el nombre a ${colega.nombre}"
                        },
                        onClick = {
                            if (puedeRenombrarse) editando = true else onRenombrarBloqueado()
                        },
                        color = if (puedeRenombrarse) Paleta.Papel else Paleta.CremaHundido,
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

private fun detalleColega(saldo: Long, puestoPorEl: Long, cuantosGastos: Int): String {
    val estado = when {
        saldo > 0 -> "le deben ${Dinero.formatea(saldo)}"
        saldo < 0 -> "debe ${Dinero.formatea(-saldo)}"
        else -> "en paz"
    }
    // El rango va por lo que ha puesto, no por su saldo: quien adelanta mucho y
    // luego cobra sigue siendo el de la tarjeta.
    return "$estado · ${Frases.rangoPagador(puestoPorEl, cuantosGastos)}"
}
