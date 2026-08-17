package com.pulgares.app.avatar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pulgares.app.ui.theme.TemaPulgares
import kotlin.math.sin

/**
 * Pinta un monigote completo. El orden de las capas importa: primero el fondo,
 * luego lo que va detras del cuerpo (patas, brazos) y al final la cara y los
 * complementos, que siempre van encima.
 *
 * [bailoteo] va de 0 a 1 y recorre un ciclo de contoneo: el mismo monigote
 * quieto (null) o dando saltitos segun donde se use.
 */
fun DrawScope.dibujaMonigote(
    monigote: Monigote,
    conFondo: Boolean = true,
    bailoteo: Float = 0f
) {
    val lado = minOf(size.width, size.height)
    val dx = (size.width - lado) / 2f
    val dy = (size.height - lado) / 2f
    val pincel = Pincel(this, lado, dx, dy, Tinta.NEGRA)

    // El ciclo de baile: se mueve en seno para que sea suave y ciclico.
    val fase = bailoteo * 2f * Math.PI.toFloat()
    val paso = sin(fase)
    val aplastado = ((sin(fase * 2f) + 1f) / 2f) * 0.6f * (if (bailoteo == 0f) 0f else 1f)
    val saludo = (sin(fase * 1.5f) + 1f) / 2f * (if (bailoteo == 0f) 0f else 1f)
    val brinco = -sin(fase * 2f) * 1.6f * (if (bailoteo == 0f) 0f else 1f)

    with(pincel) {
        if (conFondo) dibujaFondo(monigote.fondo)

        dibujaSombraSuelo(alpha = if (bailoteo == 0f) 0.16f else 0.12f)

        // Todo el bicho sube y baja junto al bailar.
        girado(paso * 2.5f, Anatomia.CX, Anatomia.PIES_Y) {
            dibujaEnSitio(monigote, aplastado, saludo, paso, brinco)
        }
    }
}

/** El monigote propiamente dicho, ya colocado y con su desplazamiento. */
private fun Pincel.dibujaEnSitio(
    monigote: Monigote,
    aplastado: Float,
    saludo: Float,
    paso: Float,
    brinco: Float
) {
    // Piernas y brazos van detras del cuerpo.
    dibujaPiernas(monigote, paso)
    dibujaBrazos(monigote, saludo)

    // El cuerpo, con su volumen.
    val cuerpo = caminoCuerpo(monigote.forma, aplastado)
    pieza(cuerpo, ColoresCuerpo.piel(monigote.color))
    dibujaVolumen(monigote, monigote.forma)
    // El contorno se repasa por encima de la sombra para que quede limpio.
    contornea(cuerpo)

    // La cara y los anadidos.
    dibujaOjos(monigote.ojos)
    dibujaBoca(monigote.boca)
    dibujaBarba(monigote.barba, monigote)
    dibujaGafas(monigote.gafas)
    dibujaMarca(monigote.marca)
    dibujaPelo(monigote.pelo, monigote)
    dibujaTocado(monigote.tocado)

    // El cachivache, en la mano que cuelga.
    dibujaCachivache(monigote.accesorio)
}

/**
 * El avatar como componente. [tamano] manda: se dibuja igual de bien a 24dp en
 * una lista que a 240dp en el editor.
 */
@Composable
fun AvatarMonigote(
    monigote: Monigote,
    modifier: Modifier = Modifier,
    tamano: Int = 48,
    conFondo: Boolean = true,
    recortadoRedondo: Boolean = true,
    baila: Boolean = false,
    descripcion: String? = null
) {
    val transicion = rememberInfiniteTransition(label = "bailoteo")
    val fase by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fase"
    )

    val forma = if (recortadoRedondo) Modifier.clip(CircleShape) else Modifier
    val etiqueta = if (descripcion == null) {
        Modifier
    } else {
        Modifier.semantics { contentDescription = descripcion }
    }

    Box(modifier = modifier.size(tamano.dp).then(forma).then(etiqueta)) {
        Canvas(modifier = Modifier.size(tamano.dp)) {
            dibujaMonigote(
                monigote = monigote,
                conFondo = conFondo,
                bailoteo = if (baila) fase else 0f
            )
        }
    }
}

/** La mascota de la casa a tamano grande, para cabeceras y estados vacios. */
@Composable
fun MascotaPulgares(
    modifier: Modifier = Modifier,
    tamano: Int = 140,
    baila: Boolean = true
) {
    AvatarMonigote(
        monigote = Monigote.ELMONIGOTE,
        modifier = modifier,
        tamano = tamano,
        conFondo = false,
        recortadoRedondo = false,
        baila = baila,
        descripcion = "El monigote del Contador de Pulgares"
    )
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaMonigotes() {
    TemaPulgares {
        Box {
            MascotaPulgares(tamano = 160, baila = false)
        }
    }
}
