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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulgares.app.ui.components.BotonPegatina
import com.pulgares.app.ui.components.BotonRedondo
import com.pulgares.app.ui.components.Pegatina
import com.pulgares.app.ui.theme.Paleta

/** Emojis para el grupo: los planes típicos donde alguien acaba debiendo algo. */
private val emojisGrupo = listOf(
    "🍻", "✈️", "🏠", "🎉", "🏖️", "⛷️", "🍕", "🎣", "🚐", "🎸", "⚽", "🎰", "🧗", "🎬"
)

/** Nombres de ejemplo para el grupo, por si no hay inspiración. */
private val ejemplos = listOf(
    "Viaje a Lisboa", "El piso", "Cañas de los viernes", "Despedida de Javi",
    "Finde en la sierra", "Cena de Navidad"
)

@Composable
fun NuevoGrupoScreen(
    onCrear: (nombre: String, emoji: String, colegas: List<String>, miNombre: String) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(emojisGrupo.first()) }
    var miNombre by remember { mutableStateOf("Yo") }
    val colegas = remember { mutableStateListOf("", "") }

    val puedeCrear = nombre.isNotBlank() &&
        miNombre.isNotBlank() &&
        colegas.count { it.isNotBlank() } >= 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonRedondo(contenido = "‹", onClick = onVolver)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nuevo grupo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Un plan, unos colegas y muchas deudas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 4.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("¿Cómo se llama el grupo?") },
                        placeholder = { Text(ejemplos.random()) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = miNombre,
                        onValueChange = { miNombre = it },
                        label = { Text("¿Y tú quién eres?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Text(
                text = "Icono",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(emojisGrupo) { candidato ->
                    val elegido = candidato == emoji
                    Pegatina(
                        color = if (elegido) Paleta.MostazaSuave else MaterialTheme.colorScheme.surface,
                        borde = if (elegido) Paleta.RosaChicle else Paleta.Tinta,
                        grosorBorde = if (elegido) 3.5.dp else 2.dp,
                        radio = 16.dp,
                        sombra = if (elegido) 4.dp else 2.dp,
                        onClick = { emoji = candidato }
                    ) {
                        Text(
                            text = candidato,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "¿Quién más va?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        itemsIndexed(colegas) { indice, valor ->
            Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 2.dp) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { colegas[indice] = it },
                        label = { Text("Colega ${indice + 1}") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (colegas.size > 1) {
                        Spacer(Modifier.width(8.dp))
                        BotonRedondo(
                            contenido = "✕",
                            onClick = { colegas.removeAt(indice) },
                            color = Paleta.RojoDeudaSuave,
                            sombra = 2.dp
                        )
                    }
                }
            }
        }

        item {
            BotonPegatina(
                texto = "Añadir otro",
                emoji = "➕",
                color = Paleta.CremaHundido,
                colorTexto = Paleta.Tinta,
                onClick = { colegas.add("") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(4.dp))
            BotonPegatina(
                texto = "Crear grupo",
                emoji = "🚀",
                habilitado = puedeCrear,
                onClick = { onCrear(nombre.trim(), emoji, colegas.toList(), miNombre.trim()) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
