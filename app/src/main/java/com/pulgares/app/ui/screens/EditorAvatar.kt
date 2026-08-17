package com.pulgares.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.avatar.Dimension
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.ui.components.BotonPegatina
import com.pulgares.app.ui.components.BotonRedondo
import com.pulgares.app.ui.components.Chapa
import com.pulgares.app.ui.components.Pegatina
import com.pulgares.app.ui.theme.Paleta

/**
 * El creador de monigotes. Arriba el bicho a lo grande (bailando), abajo las
 * pestanas de piezas y una tira de variantes por pestana. Todas las variantes
 * se pintan de verdad en su miniatura, asi que se ve lo que se elige.
 */
@Composable
fun EditorAvatarScreen(
    inicial: Monigote,
    onGuardar: (Monigote) -> Unit,
    onVolver: () -> Unit,
    titulo: String = "Tu monigote"
) {
    // Con la key: si el avatar de verdad llega despues (al restaurar la app,
    // el flujo empieza con el monigote por defecto), el editor se pone al dia en
    // vez de guardar el de por defecto encima del bueno.
    var monigote by remember(inicial) { mutableStateOf(inicial) }
    var dimension by remember { mutableStateOf(Dimension.FORMA) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ---- cabecera ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonRedondo(contenido = "‹", onClick = onVolver, descripcion = "Volver sin guardar")
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Hay ${millones(Monigote.combinaciones)} combinaciones. Suerte.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BotonRedondo(
                contenido = "🎲",
                descripcion = "Un monigote al azar",
                onClick = { monigote = Monigote.aleatorio() },
                color = Paleta.MostazaPulgar
            )
        }

        // ---- el monigote a lo grande ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Pegatina(
                color = MaterialTheme.colorScheme.surface,
                radio = 32.dp,
                sombra = 6.dp
            ) {
                Box(modifier = Modifier.padding(10.dp)) {
                    AvatarMonigote(
                        monigote = monigote,
                        tamano = 210,
                        baila = true,
                        recortadoRedondo = true,
                        descripcion = "Vista previa de tu monigote"
                    )
                }
            }
        }

        // ---- lo que llevas puesto ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Chapa(
                texto = monigote.nombreDe(Dimension.COLOR),
                color = Paleta.RosaMonigote
            )
            listOf(Dimension.PELO, Dimension.TOCADO, Dimension.GAFAS, Dimension.ACCESORIO)
                .filter { monigote.valorDe(it) != 0 }
                .forEach { d ->
                    Chapa(texto = monigote.nombreDe(d), color = Paleta.MostazaSuave)
                }
        }

        Spacer(Modifier.height(4.dp))

        // ---- pestanas de piezas ----
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Dimension.entries.toList()) { d ->
                val elegida = d == dimension
                Pegatina(
                    color = if (elegida) Paleta.RosaChicle else MaterialTheme.colorScheme.surface,
                    radio = 14.dp,
                    sombra = if (elegida) 3.dp else 2.dp,
                    onClick = { dimension = d }
                ) {
                    Text(
                        text = d.etiqueta,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (elegida) Paleta.Papel else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (elegida) FontWeight.ExtraBold else FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // ---- variantes de la pieza elegida ----
        Text(
            text = "${dimension.etiqueta}: ${monigote.nombreDe(dimension)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp,
                vertical = 10.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dimension.cuantos) { indice ->
                val elegido = monigote.valorDe(dimension) == indice
                // Cada miniatura se dibuja con el resto del monigote intacto:
                // asi se ve como queda la pieza EN TU bicho, no en uno genérico.
                val muestra = monigote.conValor(dimension, indice)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Pegatina(
                        color = if (elegido) Paleta.MostazaSuave else MaterialTheme.colorScheme.surface,
                        borde = if (elegido) Paleta.RosaChicle else Paleta.Tinta,
                        grosorBorde = if (elegido) 3.5.dp else 2.dp,
                        radio = 18.dp,
                        sombra = if (elegido) 4.dp else 2.dp,
                        onClick = { monigote = muestra }
                    ) {
                        Box(modifier = Modifier.padding(4.dp)) {
                            AvatarMonigote(
                                monigote = muestra,
                                tamano = 66,
                                conFondo = dimension == Dimension.FONDO,
                                recortadoRedondo = true,
                                descripcion = dimension.nombres[indice]
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dimension.nombres[indice],
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(80.dp),
                        maxLines = 2,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // ---- guardar ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 24.dp, top = 6.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonPegatina(
                texto = "Así me quedo",
                emoji = "✅",
                onClick = { onGuardar(monigote) },
                modifier = Modifier.weight(1f)
            )
            BotonRedondo(
                contenido = "↺",
                descripcion = "Deshacer los cambios",
                onClick = { monigote = inicial },
                color = Paleta.CremaHundido
            )
        }
    }
}

/** 2419200000 -> "2.419 millones". Para presumir sin marear con los ceros. */
private fun millones(total: Long): String = when {
    total >= 1_000_000_000_000L -> "${total / 1_000_000_000_000L} billones de"
    total >= 1_000_000_000L -> "${total / 1_000_000_000L} mil millones de"
    total >= 1_000_000L -> "${total / 1_000_000L} millones de"
    else -> "$total"
}
