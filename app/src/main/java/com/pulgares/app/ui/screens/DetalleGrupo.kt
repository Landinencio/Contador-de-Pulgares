package com.pulgares.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.avatar.MascotaPulgares
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.data.EstadoGrupo
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Dinero
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.settlement.Transferencia
import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento
import com.pulgares.app.ui.components.BotonPegatina
import com.pulgares.app.ui.components.BotonRedondo
import com.pulgares.app.ui.components.Chapa
import com.pulgares.app.ui.components.Pegatina
import com.pulgares.app.ui.theme.Paleta
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * El grupo por dentro: quien debe a quien (el plan de pagos), la lista de
 * gastos con sus pulgares, y el boton de apuntar.
 */
@Composable
fun DetalleGrupoScreen(
    estado: EstadoGrupo,
    onVolver: () -> Unit,
    onNuevoGasto: () -> Unit,
    onEditarGasto: (Gasto) -> Unit,
    onVotar: (String, Boolean) -> Unit,
    onPagar: (Transferencia) -> Unit,
    onDarToque: (Colega, Long) -> Unit,
    onBorrarGrupo: () -> Unit
) {
    val grupo = estado.grupo
    val yo = grupo.yo
    val ahora = System.currentTimeMillis()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---- cabecera ----
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonRedondo(contenido = "‹", onClick = onVolver)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${grupo.emoji} ${grupo.nombre}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${grupo.colegas.size} colegas · ${Dinero.formateaCorto(estado.totalGastado)} en total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BotonRedondo(
                    contenido = "🗑",
                    onClick = onBorrarGrupo,
                    color = Paleta.RojoDeudaSuave
                )
            }
        }

        // ---- mi situacion ----
        item {
            val mio = estado.miSituacion
            val color = when {
                mio.enPaz -> Paleta.VerdePazSuave
                mio.neto < 0 -> Paleta.RojoDeudaSuave
                else -> Paleta.MostazaSuave
            }
            val frase = when {
                estado.enPaz -> Frases.para(Momento.EN_PAZ, semilla = grupo.id.hashCode().toLong())
                mio.neto < 0 -> Frases.para(
                    Momento.CABECERA_DEBO,
                    cuanto = Dinero.formatea(mio.deboCentimos),
                    semilla = mio.deboCentimos
                )
                mio.neto > 0 -> Frases.para(
                    Momento.TE_DEBEN,
                    quien = yo?.nombre ?: "",
                    cuanto = Dinero.formatea(mio.meDebenCentimos),
                    semilla = mio.meDebenCentimos
                )
                else -> Frases.para(Momento.EN_PAZ, semilla = 5)
            }
            Pegatina(color = color, radio = 24.dp, sombra = 5.dp, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = frase,
                    style = MaterialTheme.typography.titleLarge,
                    color = Paleta.Tinta,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // ---- plan de pagos ----
        if (estado.plan.isNotEmpty()) {
            item {
                Text(
                    text = "Quién paga a quién",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "El mínimo de bizums para dejarlo todo a cero",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(estado.plan) { transferencia ->
                FilaTransferencia(
                    transferencia = transferencia,
                    estado = estado,
                    soyElQuePaga = transferencia.deQuienId == yo?.id,
                    onPagar = { onPagar(transferencia) }
                )
            }
        }

        // ---- morosos de leyenda ----
        val morosos = estado.saldos
            .filter { it.esDeudor && it.colegaId != yo?.id }
            .mapNotNull { saldo ->
                val colega = grupo.colega(saldo.colegaId) ?: return@mapNotNull null
                val dias = estado.diasDeudaDe(saldo.colegaId, ahora)
                Triple(colega, -saldo.neto, dias)
            }
            .filter { it.third >= 7 }
            .sortedByDescending { it.third }

        if (morosos.isNotEmpty()) {
            item {
                Text(
                    text = "Salón de la fama",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(morosos) { (colega, deuda, dias) ->
                Pegatina(modifier = Modifier.fillMaxWidth(), color = Paleta.MostazaSuave, sombra = 3.dp) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarMonigote(
                            monigote = avatarDe(colega),
                            tamano = 44,
                            descripcion = "Monigote de ${colega.nombre}"
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Frases.rangoMoroso(deuda, dias),
                                style = MaterialTheme.typography.titleMedium,
                                color = Paleta.Tinta
                            )
                            Text(
                                text = Frases.para(
                                    Momento.MOROSO_LEYENDA,
                                    quien = colega.nombre,
                                    cuanto = Dinero.formatea(deuda),
                                    dias = dias,
                                    semilla = colega.id.hashCode().toLong()
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Paleta.TintaSuave
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        BotonRedondo(
                            contenido = "👉",
                            onClick = { onDarToque(colega, deuda) },
                            color = Paleta.RosaChicle
                        )
                    }
                }
            }
        }

        // ---- gastos ----
        item {
            Text(
                text = "Gastos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (estado.gastos.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MascotaPulgares(tamano = 120)
                    Text(
                        text = Frases.para(Momento.SIN_GASTOS, semilla = grupo.id.hashCode().toLong()),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(estado.gastos, key = { it.id }) { gasto ->
                FilaGasto(
                    gasto = gasto,
                    estado = estado,
                    miId = yo?.id,
                    onClick = { onEditarGasto(gasto) },
                    onVotar = { arriba -> onVotar(gasto.id, arriba) }
                )
            }
        }

        // ---- bizums ya hechos ----
        if (estado.pagos.isNotEmpty()) {
            item {
                Text(
                    text = "Bizums registrados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(estado.pagos) { pago ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💸 ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${grupo.nombreDe(pago.deQuienId)} → ${grupo.nombreDe(pago.aQuienId)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = Dinero.formatea(pago.importeCentimos),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            BotonPegatina(
                texto = "Apuntar gasto",
                emoji = "💸",
                onClick = onNuevoGasto,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilaTransferencia(
    transferencia: Transferencia,
    estado: EstadoGrupo,
    soyElQuePaga: Boolean,
    onPagar: () -> Unit
) {
    val grupo = estado.grupo
    Pegatina(
        modifier = Modifier.fillMaxWidth(),
        color = if (soyElQuePaga) Paleta.RojoDeudaSuave else MaterialTheme.colorScheme.surface,
        sombra = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarMonigote(
                monigote = avatarDe(grupo.colega(transferencia.deQuienId)),
                tamano = 40,
                descripcion = grupo.nombreDe(transferencia.deQuienId)
            )
            Text(text = " → ", style = MaterialTheme.typography.titleMedium)
            AvatarMonigote(
                monigote = avatarDe(grupo.colega(transferencia.aQuienId)),
                tamano = 40,
                descripcion = grupo.nombreDe(transferencia.aQuienId)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${grupo.nombreDe(transferencia.deQuienId)} debe a ${grupo.nombreDe(transferencia.aQuienId)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = Dinero.formatea(transferencia.importeCentimos),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            BotonRedondo(
                contenido = if (soyElQuePaga) "Pagar" else "Cobrado",
                onClick = onPagar,
                color = if (soyElQuePaga) Paleta.VerdePaz else Paleta.CremaHundido
            )
        }
    }
}

@Composable
private fun FilaGasto(
    gasto: Gasto,
    estado: EstadoGrupo,
    miId: String?,
    onClick: () -> Unit,
    onVotar: (Boolean) -> Unit
) {
    val grupo = estado.grupo
    val loMio = gasto.deudas()[miId] ?: 0L
    val medalla = Frases.medallaPulgares(gasto.saldoPulgares)

    Pegatina(modifier = Modifier.fillMaxWidth(), onClick = onClick, sombra = 3.dp) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(38.dp), contentAlignment = Alignment.Center) {
                    Text(text = gasto.categoria.emoji, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gasto.concepto,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Puso ${grupo.nombreDe(gasto.pagadorId)} · ${fecha(gasto.fechaMillis)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Dinero.formatea(gasto.importeCentimos),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (loMio > 0) {
                        Text(
                            text = "tú ${Dinero.formatea(loMio)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Los pulgares: la firma de la casa.
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonRedondo(
                    contenido = "👍 ${gasto.pulgaresArriba.size}",
                    onClick = { onVotar(true) },
                    color = if (miId != null && miId in gasto.pulgaresArriba) {
                        Paleta.VerdePazSuave
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    sombra = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                BotonRedondo(
                    contenido = "👎 ${gasto.pulgaresAbajo.size}",
                    onClick = { onVotar(false) },
                    color = if (miId != null && miId in gasto.pulgaresAbajo) {
                        Paleta.RojoDeudaSuave
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    sombra = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                if (medalla != null) {
                    Chapa(texto = medalla, color = Paleta.MostazaPulgar)
                }
            }
        }
    }
}

/** El avatar guardado del colega, o uno inventado a partir de su nombre. */
fun avatarDe(colega: Colega?): Monigote {
    if (colega == null) return Monigote.ELMONIGOTE
    return Monigote.parse(colega.avatar) ?: Monigote.desdeSemilla(colega.id + colega.nombre)
}

private val formatoFecha = DateTimeFormatter.ofPattern("d MMM")

private fun fecha(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatoFecha)
