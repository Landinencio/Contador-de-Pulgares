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
import com.pulgares.app.avatar.Monigote
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

/**
 * Crear un grupo: nombre e icono, y ya. Tú entras con tu perfil; el resto de la
 * gente pide entrar con el código (cada uno con su nombre y su monigote), o se
 * añade a mano en los ajustes para grupos sin nube.
 */
@Composable
fun NuevoGrupoScreen(
    miNombre: String,
    miAvatar: Monigote,
    onCrear: (nombre: String, emoji: String) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf(emojisGrupo.first()) }
    // Sin remember, el ejemplo saltaba a otro en cada tecla pulsada.
    val ejemplo = remember { ejemplos.random() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonRedondo(contenido = "‹", onClick = onVolver, descripcion = "Volver a la portada")
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nuevo grupo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Un plan y un nombre. La gente ya vendrá sola.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 4.dp) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("¿Cómo se llama el grupo?") },
                    placeholder = { Text(ejemplo) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
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

        // ---- quién entra de primeras: tú, con tu perfil ----
        item {
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
                        monigote = miAvatar,
                        tamano = 44,
                        conFondo = false,
                        descripcion = "Tu monigote"
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = miNombre,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Entras tú. Los demás piden entrar con el código, " +
                                "cada uno con su nombre.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            BotonPegatina(
                texto = "Crear grupo",
                emoji = "🚀",
                habilitado = nombre.isNotBlank(),
                onClick = { onCrear(nombre.trim(), emoji) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
