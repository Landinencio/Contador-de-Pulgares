package com.pulgares.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pulgares.app.ui.theme.Paleta

/**
 * Aviso antes de hacer algo que no se puede deshacer. Borrar un grupo se lleva
 * por delante todos sus gastos, asi que preguntar no es opcional.
 */
@Composable
fun DialogoConfirmar(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    emoji: String = "🗑",
    colorConfirmar: Color = Paleta.RojoDeuda
) {
    Dialog(onDismissRequest = onCancelar) {
        Pegatina(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            radio = 26.dp,
            sombra = 6.dp
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(text = emoji, style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotonPegatina(
                        texto = "Mejor no",
                        color = Paleta.CremaHundido,
                        colorTexto = Paleta.Tinta,
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPegatina(
                        texto = textoConfirmar,
                        color = colorConfirmar,
                        onClick = onConfirmar,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
