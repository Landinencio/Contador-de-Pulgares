package com.pulgares.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.ui.components.BotonPegatina
import com.pulgares.app.ui.components.BotonRedondo
import com.pulgares.app.ui.components.Pegatina
import com.pulgares.app.ui.theme.Paleta

/**
 * El perfil: tu nombre y tu monigote. Aparece la primera vez que se abre la app
 * (sin perfil no se puede seguir: es lo que viaja al pedir entrar en un grupo y
 * lo que te identifica al crear uno), y luego vive detras del avatar de la
 * portada para retocarlo.
 *
 * Es la MISMA pantalla para el primer arranque y para editar: solo cambian el
 * titulo y el texto del boton.
 */
@Composable
fun PerfilScreen(
    nombreInicial: String,
    avatarInicial: Monigote,
    esPrimeraVez: Boolean,
    onGuardar: (nombre: String, avatar: Monigote) -> Unit,
    onVolver: (() -> Unit)? = null
) {
    var nombre by rememberSaveable { mutableStateOf(nombreInicial) }
    // El monigote se guarda serializado para sobrevivir al giro de pantalla.
    var avatarTexto by rememberSaveable { mutableStateOf(avatarInicial.serializa()) }
    var editandoMonigote by rememberSaveable { mutableStateOf(false) }
    val avatar = Monigote.parse(avatarTexto) ?: avatarInicial

    if (editandoMonigote) {
        EditorAvatarScreen(
            inicial = avatar,
            titulo = "Tu monigote",
            onGuardar = { nuevo ->
                avatarTexto = nuevo.serializa()
                editandoMonigote = false
            },
            onVolver = { editandoMonigote = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 24.dp, top = 16.dp, bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onVolver != null) {
                BotonRedondo(contenido = "‹", onClick = onVolver, descripcion = "Volver sin guardar")
                Spacer(Modifier.width(12.dp))
            }
            Column {
                Text(
                    text = if (esPrimeraVez) "¡Hola! ¿Quién eres?" else "Tu perfil",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (esPrimeraVez) {
                        "Con este nombre y este monigote te verán tus colegas en los grupos."
                    } else {
                        "Tu nombre y tu monigote, los mismos en todos los grupos."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- el monigote, grande y bailando ----
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Pegatina(radio = 32.dp, sombra = 6.dp, color = Paleta.RosaMonigote) {
                Box(modifier = Modifier.padding(10.dp)) {
                    AvatarMonigote(
                        monigote = avatar,
                        tamano = 190,
                        conFondo = false,
                        baila = true,
                        descripcion = "Tu monigote"
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            BotonRedondo(
                contenido = "🎲 Sorpréndeme",
                descripcion = "Un monigote al azar",
                color = Paleta.MostazaPulgar,
                onClick = { avatarTexto = Monigote.aleatorio().serializa() }
            )
            BotonRedondo(
                contenido = "🎨 Retocar",
                descripcion = "Abrir el editor del monigote",
                color = Paleta.RosaChicle,
                onClick = { editandoMonigote = true }
            )
        }

        Spacer(Modifier.height(24.dp))

        Pegatina(modifier = Modifier.fillMaxWidth(), sombra = 4.dp) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Tu nombre") },
                placeholder = { Text("El que verán tus colegas") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        BotonPegatina(
            texto = if (esPrimeraVez) "¡Este soy yo!" else "Guardar",
            emoji = "✅",
            habilitado = nombre.isNotBlank(),
            onClick = { onGuardar(nombre.trim(), avatar) },
            modifier = Modifier.fillMaxWidth()
        )

        if (esPrimeraVez) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Sin cuentas ni correos: esto se queda en tu móvil.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
