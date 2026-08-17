package com.pulgares.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulgares.app.ui.theme.Paleta

/**
 * Los ladrillos visuales de la app: todo son pegatinas de rotulador, con borde
 * negro gordo y una sombra dura desplazada (sin difuminar). Es lo que le da el
 * aire de comic y lo que la distingue de una app de banco.
 *
 * La sombra se pinta con [sombraDura], que dibuja DETRAS del propio componente
 * y se sale de sus limites: quien la use debe dejar unos dp de padding para que
 * no la recorte el contenedor.
 */
fun Modifier.sombraDura(
    desplazamiento: Dp,
    radio: Dp,
    color: Color = Paleta.Tinta
): Modifier = drawBehind {
    val d = desplazamiento.toPx()
    if (d <= 0f) return@drawBehind
    drawRoundRect(
        color = color,
        topLeft = Offset(d, d),
        size = size,
        cornerRadius = CornerRadius(radio.toPx())
    )
}

/** Tarjeta-pegatina: el contenedor por defecto de casi todo. */
@Composable
fun Pegatina(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    borde: Color = Paleta.Tinta,
    grosorBorde: Dp = 2.5.dp,
    sombra: Dp = 4.dp,
    radio: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    contenido: @Composable () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    val pulsada by interaccion.collectIsPressedAsState()
    // Al pulsar, la pegatina se hunde sobre su propia sombra.
    val hundido by animateDpAsState(
        targetValue = if (pulsada && onClick != null) sombra else 0.dp,
        label = "hundido"
    )
    val forma = RoundedCornerShape(radio)

    Box(
        modifier = modifier
            .offset(x = hundido, y = hundido)
            .sombraDura(sombra - hundido, radio)
            .clip(forma)
            .background(color)
            .border(BorderStroke(grosorBorde, borde), forma)
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interaccion, indication = null) { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        contenido()
    }
}

/** Boton gordo de pegatina: el de "Apuntar gasto" y compania. */
@Composable
fun BotonPegatina(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Paleta.RosaChicle,
    colorTexto: Color = Paleta.Papel,
    emoji: String? = null,
    habilitado: Boolean = true,
    sombra: Dp = 5.dp
) {
    val interaccion = remember { MutableInteractionSource() }
    val pulsado by interaccion.collectIsPressedAsState()
    val hundido by animateDpAsState(
        targetValue = if (pulsado && habilitado) sombra else 0.dp,
        label = "hundidoBoton"
    )
    val radio = 18.dp
    val forma = RoundedCornerShape(radio)
    val fondo = if (habilitado) color else Paleta.CremaHundido

    Box(
        modifier = modifier
            .offset(x = hundido, y = hundido)
            .sombraDura(sombra - hundido, radio)
            .clip(forma)
            .background(fondo)
            .border(BorderStroke(2.5.dp, Paleta.Tinta), forma)
            .clickable(
                interactionSource = interaccion,
                indication = null,
                enabled = habilitado
            ) { onClick() }
            .padding(vertical = 14.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (emoji != null) {
                Text(text = "$emoji ", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                color = if (habilitado) colorTexto else Paleta.TintaSuave,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Etiqueta pequena de colorines: categorias, rangos, estados. */
@Composable
fun Chapa(
    texto: String,
    color: Color,
    modifier: Modifier = Modifier,
    colorTexto: Color = Paleta.Tinta
) {
    val forma = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(forma)
            .background(color)
            .border(BorderStroke(1.8.dp, Paleta.Tinta), forma)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = colorTexto
        )
    }
}

/** Botoncito redondo de icono (pulgares, flechas del editor, cerrar). */
@Composable
fun BotonRedondo(
    contenido: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Paleta.Papel,
    tamanoTexto: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    sombra: Dp = 3.dp
) {
    val interaccion = remember { MutableInteractionSource() }
    val pulsado by interaccion.collectIsPressedAsState()
    val hundido by animateDpAsState(
        targetValue = if (pulsado) sombra else 0.dp,
        label = "hundidoRedondo"
    )
    val forma = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .offset(x = hundido, y = hundido)
            .sombraDura(sombra - hundido, 50.dp)
            .clip(forma)
            .background(color)
            .border(BorderStroke(2.dp, Paleta.Tinta), forma)
            .clickable(interactionSource = interaccion, indication = null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = contenido, style = tamanoTexto)
    }
}
