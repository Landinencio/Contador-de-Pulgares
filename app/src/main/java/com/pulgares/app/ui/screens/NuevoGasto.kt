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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.domain.model.Categoria
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Dinero
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Reparto
import com.pulgares.app.ui.components.BotonPegatina
import com.pulgares.app.ui.components.BotonRedondo
import com.pulgares.app.ui.components.Chapa
import com.pulgares.app.ui.components.DialogoConfirmar
import com.pulgares.app.ui.components.Pegatina
import com.pulgares.app.ui.theme.Paleta

/** Los tres modos de partir la cuenta que ofrece la pantalla. */
private enum class ModoReparto(val etiqueta: String, val pista: String) {
    ESCOTE("A escote", "El importe entre todos los marcados"),
    PARTES("Por partes", "Uno cuenta doble si repitió"),
    EXACTO("A dedo", "Cada uno pone lo suyo, al céntimo")
}

/**
 * Apuntar (o editar) un gasto: concepto, importe, quién puso el dinero, entre
 * quiénes se reparte y cómo.
 */
@Composable
fun NuevoGastoScreen(
    colegas: List<Colega>,
    gastoExistente: Gasto?,
    onGuardar: (concepto: String, importe: Long, pagadorId: String, categoria: Categoria, reparto: Reparto, nota: String?) -> Unit,
    onBorrar: (() -> Unit)?,
    onVolver: () -> Unit
) {
    val yo = colegas.firstOrNull { it.soyYo }

    // rememberSaveable: girar el móvil recrea la Activity, y con remember normal
    // el formulario se quedaba en blanco a media faena.
    var concepto by rememberSaveable { mutableStateOf(gastoExistente?.concepto ?: "") }
    var importeTexto by rememberSaveable {
        mutableStateOf(
            gastoExistente?.let { Dinero.formatea(it.importeCentimos, conSimbolo = false) } ?: ""
        )
    }
    var pagadorId by rememberSaveable {
        mutableStateOf(gastoExistente?.pagadorId ?: yo?.id ?: colegas.firstOrNull()?.id ?: "")
    }
    var categoria by rememberSaveable {
        mutableStateOf(gastoExistente?.categoria ?: Categoria.BIRRAS)
    }
    var nota by rememberSaveable { mutableStateOf(gastoExistente?.nota ?: "") }
    var borrando by rememberSaveable { mutableStateOf(false) }

    val repartoInicial = gastoExistente?.reparto
    var modo by rememberSaveable {
        mutableStateOf(
            when (repartoInicial) {
                is Reparto.PorPartes -> ModoReparto.PARTES
                is Reparto.Exacto -> ModoReparto.EXACTO
                else -> ModoReparto.ESCOTE
            }
        )
    }

    // Quién entra en el reparto (modo escote).
    val marcados = remember {
        mutableStateMapOf<String, Boolean>().apply {
            val implicados = repartoInicial?.implicados
            colegas.forEach { colega ->
                put(colega.id, implicados == null || colega.id in implicados)
            }
        }
    }
    // Partes de cada uno (modo por partes).
    val partes = remember {
        mutableStateMapOf<String, Int>().apply {
            val pesos = (repartoInicial as? Reparto.PorPartes)?.pesos
            colegas.forEach { colega -> put(colega.id, pesos?.get(colega.id) ?: 1) }
        }
    }
    // Importes exactos (modo a dedo), como texto para poder escribir a gusto.
    val exactos = remember {
        mutableStateMapOf<String, String>().apply {
            val importes = (repartoInicial as? Reparto.Exacto)?.importes
            colegas.forEach { colega ->
                put(colega.id, importes?.get(colega.id)?.let { Dinero.formatea(it, false) } ?: "")
            }
        }
    }

    val importeCentimos = Dinero.parse(importeTexto) ?: 0L
    val reparto = construyeReparto(modo, colegas, marcados, partes, exactos)
    val sumaExactos = if (modo == ModoReparto.EXACTO) {
        colegas.sumOf { Dinero.parse(exactos[it.id] ?: "") ?: 0L }
    } else {
        importeCentimos
    }
    val cuadra = importeCentimos > 0 &&
        reparto.implicados.isNotEmpty() &&
        (modo != ModoReparto.EXACTO || sumaExactos == importeCentimos)

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
                    text = if (gastoExistente == null) "Nuevo gasto" else "Editar gasto",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (onBorrar != null) {
                    BotonRedondo(
                        contenido = "🗑",
                        descripcion = "Borrar este gasto",
                        onClick = { borrando = true },
                        color = Paleta.RojoDeudaSuave
                    )
                }
            }
        }

        // ---- concepto e importe ----
        item {
            Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 4.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = concepto,
                        onValueChange = { concepto = it },
                        label = { Text("¿En qué se fue el dinero?") },
                        placeholder = { Text("Cañas del viernes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importeTexto,
                        onValueChange = { importeTexto = it.replace(".", ",") },
                        label = { Text("Importe") },
                        placeholder = { Text("24,50") },
                        suffix = { Text("€") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = importeTexto.isNotBlank() && Dinero.parse(importeTexto) == null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Las pesetas, en vivo mientras se escribe el importe.
                    if (importeCentimos > 0) {
                        Text(
                            text = "Que son ${Dinero.formateaPesetas(importeCentimos)} de las de antes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }
                }
            }
        }

        // ---- categoria ----
        item { Titulillo("Categoría") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Categoria.entries.toList()) { cat ->
                    val elegida = cat == categoria
                    val fondo = Paleta.categorias[cat.ordinal]
                    Pegatina(
                        color = if (elegida) fondo else MaterialTheme.colorScheme.surface,
                        radio = 14.dp,
                        sombra = if (elegida) 3.dp else 2.dp,
                        onClick = { categoria = cat }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${cat.emoji} ", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = cat.etiqueta,
                                style = MaterialTheme.typography.labelLarge,
                                // Elegida: tinta segun el color de la ficha, que es
                                // fijo. Sin elegir, la del tema sobre el tema.
                                color = if (elegida) {
                                    Paleta.textoSobre(fondo)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (elegida) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // ---- quien pago ----
        item { Titulillo("¿Quién puso el dinero?") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(colegas) { colega ->
                    val elegido = colega.id == pagadorId
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Pegatina(
                            color = if (elegido) Paleta.MostazaSuave else MaterialTheme.colorScheme.surface,
                            borde = if (elegido) Paleta.RosaChicle else Paleta.Tinta,
                            grosorBorde = if (elegido) 3.5.dp else 2.dp,
                            radio = 50.dp,
                            sombra = if (elegido) 4.dp else 2.dp,
                            onClick = { pagadorId = colega.id }
                        ) {
                            AvatarMonigote(
                                monigote = avatarDe(colega),
                                tamano = 54,
                                descripcion = colega.nombre
                            )
                        }
                        Text(
                            text = colega.nombre,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---- como se reparte ----
        item { Titulillo("¿Cómo se reparte?") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModoReparto.entries.forEach { m ->
                    val elegido = m == modo
                    Pegatina(
                        color = if (elegido) Paleta.RosaChicle else MaterialTheme.colorScheme.surface,
                        radio = 14.dp,
                        sombra = if (elegido) 3.dp else 2.dp,
                        onClick = { modo = m }
                    ) {
                        Text(
                            text = m.etiqueta,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (elegido) Paleta.Papel else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Text(
                text = modo.pista,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ---- filas por colega segun el modo ----
        items(colegas, key = { it.id }) { colega ->
            FilaReparto(
                colega = colega,
                modo = modo,
                marcado = marcados[colega.id] ?: true,
                partes = partes[colega.id] ?: 1,
                exacto = exactos[colega.id] ?: "",
                leToca = if (importeCentimos <= 0) {
                    0L
                } else {
                    // Con el id real: de él depende a quién le cae el céntimo
                    // suelto, así que con un id inventado la previsión mentía.
                    previsionDe(reparto, importeCentimos, colega.id, gastoExistente?.id)
                },
                onMarcar = { marcados[colega.id] = it },
                onPartes = { partes[colega.id] = it.coerceIn(0, 20) },
                onExacto = { exactos[colega.id] = it.replace(".", ",") }
            )
        }

        // ---- aviso de descuadre en modo exacto ----
        if (modo == ModoReparto.EXACTO && importeCentimos > 0 && sumaExactos != importeCentimos) {
            item {
                val diferencia = importeCentimos - sumaExactos
                Pegatina(color = Paleta.RojoDeudaSuave, sombra = 3.dp, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (diferencia > 0) {
                            "Faltan ${Dinero.formatea(diferencia)} por repartir"
                        } else {
                            "Os habéis pasado ${Dinero.formatea(-diferencia)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Paleta.Tinta,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        item {
            Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 3.dp) {
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    placeholder = { Text("La ronda que nadie recuerda") },
                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                )
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            BotonPegatina(
                texto = if (gastoExistente == null) "Apuntar" else "Guardar",
                emoji = "✍️",
                habilitado = cuadra,
                onClick = {
                    onGuardar(
                        concepto,
                        importeCentimos,
                        pagadorId,
                        categoria,
                        reparto,
                        nota.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (borrando && onBorrar != null) {
        DialogoConfirmar(
            titulo = "¿Borrar el gasto?",
            mensaje = "«${gastoExistente?.concepto.orEmpty()}» por " +
                "${Dinero.formatea(gastoExistente?.importeCentimos ?: 0L)} desaparece del grupo, " +
                "y los saldos de todos se recalculan sin él.",
            textoConfirmar = "Borrar",
            onConfirmar = {
                borrando = false
                onBorrar()
            },
            onCancelar = { borrando = false }
        )
    }
}

@Composable
private fun Titulillo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun FilaReparto(
    colega: Colega,
    modo: ModoReparto,
    marcado: Boolean,
    partes: Int,
    exacto: String,
    leToca: Long,
    onMarcar: (Boolean) -> Unit,
    onPartes: (Int) -> Unit,
    onExacto: (String) -> Unit
) {
    Pegatina(
        modifier = Modifier.fillMaxWidth(),
        color = if (modo == ModoReparto.ESCOTE && !marcado) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        sombra = 2.dp,
        onClick = if (modo == ModoReparto.ESCOTE) {
            { onMarcar(!marcado) }
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarMonigote(monigote = avatarDe(colega), tamano = 40, descripcion = colega.nombre)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = colega.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (modo != ModoReparto.EXACTO && (modo != ModoReparto.ESCOTE || marcado)) {
                    Text(
                        text = "le toca ${Dinero.formatea(leToca)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when (modo) {
                ModoReparto.ESCOTE -> Chapa(
                    texto = if (marcado) "Dentro" else "Fuera",
                    color = if (marcado) Paleta.VerdePazSuave else Paleta.CremaHundido
                )

                ModoReparto.PARTES -> Row(verticalAlignment = Alignment.CenterVertically) {
                    BotonRedondo(
                        contenido = "−",
                        descripcion = "Una parte menos para ${colega.nombre}",
                        onClick = { onPartes(partes - 1) },
                        sombra = 2.dp
                    )
                    Box(
                        modifier = Modifier.width(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "$partes", style = MaterialTheme.typography.titleLarge)
                    }
                    BotonRedondo(
                        contenido = "+",
                        descripcion = "Una parte más para ${colega.nombre}",
                        onClick = { onPartes(partes + 1) },
                        sombra = 2.dp
                    )
                }

                ModoReparto.EXACTO -> OutlinedTextField(
                    value = exacto,
                    onValueChange = onExacto,
                    placeholder = { Text("0,00") },
                    suffix = { Text("€") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(130.dp)
                )
            }
        }
    }
}

/** Arma el reparto que toca según el modo elegido. */
private fun construyeReparto(
    modo: ModoReparto,
    colegas: List<Colega>,
    marcados: Map<String, Boolean>,
    partes: Map<String, Int>,
    exactos: Map<String, String>
): Reparto = when (modo) {
    ModoReparto.ESCOTE -> Reparto.Escote(
        colegas.map { it.id }.filter { marcados[it] != false }
    )

    ModoReparto.PARTES -> Reparto.PorPartes(
        colegas.map { it.id }
            .associateWith { (partes[it] ?: 1).coerceAtLeast(0) }
            .filterValues { it > 0 }
    )

    ModoReparto.EXACTO -> Reparto.Exacto(
        colegas.map { it.id }
            .associateWith { Dinero.parse(exactos[it] ?: "") ?: 0L }
            .filterValues { it > 0 }
    )
}

/** Cuánto le tocaría a un colega con el reparto actual (para la vista previa). */
private fun previsionDe(
    reparto: Reparto,
    importe: Long,
    colegaId: String,
    gastoId: String?
): Long {
    val falso = Gasto(
        id = gastoId ?: "",
        grupoId = "",
        concepto = "",
        importeCentimos = importe,
        pagadorId = "",
        fechaMillis = 0L,
        reparto = reparto
    )
    return falso.deudas()[colegaId] ?: 0L
}
