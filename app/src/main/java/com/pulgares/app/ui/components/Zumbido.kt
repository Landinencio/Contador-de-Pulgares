package com.pulgares.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento
import com.pulgares.app.ui.PulgaresViewModel
import com.pulgares.app.ui.theme.Paleta
import kotlinx.coroutines.delay

/**
 * El zumbido en pantalla: quién te sacude, a lo grande y con su monigote
 * bailando. La vibración y el temblor de pantalla los pone quien lo muestra
 * (MainActivity); esto es el cartel.
 *
 * Se quita solo a los cuatro segundos, o tocando donde sea.
 */
@Composable
fun ZumbidoOverlay(
    zumbido: PulgaresViewModel.ZumbidoRecibido,
    onVisto: () -> Unit
) {
    LaunchedEffect(zumbido.id) {
        delay(4_000)
        onVisto()
    }

    val interaccion = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paleta.Tinta.copy(alpha = 0.72f))
            .clickable(interactionSource = interaccion, indication = null) { onVisto() },
        contentAlignment = Alignment.Center
    ) {
        Pegatina(
            color = Paleta.MostazaPulgar,
            radio = 30.dp,
            sombra = 8.dp,
            modifier = Modifier.padding(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡ZUMBIDO!",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = Paleta.Tinta
                )
                Spacer(Modifier.height(10.dp))
                AvatarMonigote(
                    monigote = Monigote.parse(zumbido.avatar)
                        ?: Monigote.desdeSemilla(zumbido.nombre),
                    tamano = 150,
                    conFondo = false,
                    baila = true,
                    descripcion = "Monigote de ${zumbido.nombre}, zumbándote"
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = Frases.para(
                        Momento.ZUMBIDO,
                        quien = zumbido.nombre,
                        semilla = zumbido.id
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = Paleta.Tinta,
                    textAlign = TextAlign.Center
                )
                if (zumbido.veces > 1) {
                    Spacer(Modifier.height(8.dp))
                    Chapa(
                        texto = "×${zumbido.veces} — insiste el elemento",
                        color = Paleta.RosaChicle,
                        colorTexto = Paleta.Papel
                    )
                }
            }
        }
    }
}
