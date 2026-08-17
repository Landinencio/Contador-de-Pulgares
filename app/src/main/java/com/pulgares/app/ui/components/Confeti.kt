package com.pulgares.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import com.pulgares.app.ui.theme.Paleta
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/**
 * Confeti de celebracion. Se lanza cuando un grupo se queda a cero: es el unico
 * momento de la app que merece fiesta.
 *
 * [dispara] es un interruptor: cuando pasa a true cae el confeti y se apaga solo
 * a los pocos segundos, avisando por [onFin] para que quien lo llamo baje el flag.
 */
@Composable
fun LluviaDeConfeti(dispara: Boolean, onFin: () -> Unit) {
    if (!dispara) return

    val ancho = LocalConfiguration.current.screenWidthDp
    val colores = remember {
        listOf(
            Paleta.RosaChicle, Paleta.MostazaPulgar, Paleta.VerdePaz,
            Paleta.AzulPitufo, Paleta.MoradoUva, Paleta.RosaMonigote
        ).map { it.toArgb() }
    }

    // Dos chorros desde las esquinas de arriba, como los cañones de fiesta.
    val fiesta = remember(colores) {
        listOf(
            Party(
                speed = 12f,
                maxSpeed = 34f,
                damping = 0.9f,
                angle = 60,
                spread = 55,
                colors = colores,
                emitter = Emitter(duration = 900, TimeUnit.MILLISECONDS).max(120),
                position = Position.Relative(0.0, 0.15)
            ),
            Party(
                speed = 12f,
                maxSpeed = 34f,
                damping = 0.9f,
                angle = 120,
                spread = 55,
                colors = colores,
                emitter = Emitter(duration = 900, TimeUnit.MILLISECONDS).max(120),
                position = Position.Relative(1.0, 0.15)
            )
        )
    }

    LaunchedEffect(dispara, ancho) {
        delay(3_500)
        onFin()
    }

    KonfettiView(modifier = Modifier.fillMaxSize(), parties = fiesta)
}

/**
 * Detecta el momento exacto en que [enPaz] pasa de false a true y devuelve un
 * interruptor para el confeti. Si el grupo ya estaba en paz al entrar, no se
 * celebra nada: la fiesta es por saldar, no por estar saldado.
 */
@Composable
fun recuerdaCelebracion(enPaz: Boolean): Pair<Boolean, () -> Unit> {
    var celebrar by remember { mutableStateOf(false) }
    var estabaEnPaz by remember { mutableStateOf(enPaz) }

    LaunchedEffect(enPaz) {
        if (enPaz && !estabaEnPaz) celebrar = true
        estabaEnPaz = enPaz
    }

    return celebrar to { celebrar = false }
}
