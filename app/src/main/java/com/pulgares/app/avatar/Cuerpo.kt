package com.pulgares.app.avatar

import androidx.compose.ui.graphics.Color

/**
 * Anatomia del monigote. Todo en coordenadas 0..100 (ver [Pincel]): el bicho
 * es un blob con patas donde la cabeza y el tronco son la misma pieza, igual
 * que el monigote rosa original.
 */
object Anatomia {
    const val CX = 50f          // eje central
    const val CIMA = 20f        // coronilla
    const val SUELO = 74f       // donde acaba el cuerpo y empiezan las patas
    const val OJOS_Y = 39f      // altura de los ojos
    const val OJOS_SEP = 11.5f  // separacion respecto al eje
    const val BOCA_Y = 55f      // altura de la boca
    const val PIES_Y = 93f      // suelo de verdad
    const val MANO_IZQ_X = 19f  // mano que sujeta cachivaches
    const val MANO_IZQ_Y = 64f
    const val MANO_DER_X = 82f  // mano levantada, la del saludo
    const val MANO_DER_Y = 31f
}

/** Los 14 colores de cuerpo, cada uno con su sombra para el volumen. */
object ColoresCuerpo {

    private val lista = listOf(
        Color(0xFFFF9EC0) to Color(0xFFE77BA2), // rosa chicle
        Color(0xFFF3C2CD) to Color(0xFFDFA6B4), // rosa monigote (el original)
        Color(0xFFA8D84E) to Color(0xFF86B62F), // verde moco
        Color(0xFF7FC4FF) to Color(0xFF5AA5E8), // azul pitufo
        Color(0xFFFFD84D) to Color(0xFFE8BC2A), // amarillo pollo
        Color(0xFFBFA0FF) to Color(0xFF9E7DE8), // morado uva
        Color(0xFFFF9C5B) to Color(0xFFE87B38), // naranja gamba
        Color(0xFFC99060) to Color(0xFFA87343), // marron croqueta
        Color(0xFFB9BFC4) to Color(0xFF98A0A6), // gris zombi
        Color(0xFFFBF7F0) to Color(0xFFDCD5C9), // blanco fantasma
        Color(0xFFFF6F63) to Color(0xFFE04A3E), // rojo tomate
        Color(0xFF57D6C9) to Color(0xFF33B5A8), // turquesa piscina
        Color(0xFF4A4550) to Color(0xFF332F38), // negro carbon
        Color(0xFFF0C33C) to Color(0xFFD1A21C)  // dorado nuevo rico
    )

    fun piel(indice: Int): Color = lista[indice.mod(lista.size)].first
    fun sombra(indice: Int): Color = lista[indice.mod(lista.size)].second

    /** Para el pelo y detalles: negro salvo si el cuerpo ya es oscuro. */
    fun contraste(indice: Int): Color =
        if (indice == 12) Color(0xFFEDE7F2) else Tinta.NEGRA
}

object Tinta {
    val NEGRA = Color(0xFF17161A)
    val BLANCA = Color(0xFFFFFDF8)
    val ROJA = Color(0xFFE8443C)
    val ROSA = Color(0xFFFF7BA8)
    val AMARILLA = Color(0xFFFFC53D)
    val AZUL = Color(0xFF4C9BE8)
    val VERDE = Color(0xFF3EBB7C)
    val MARRON = Color(0xFF8A5A33)
    val GRIS = Color(0xFF8B8794)
    val DORADA = Color(0xFFE8B424)
    val CARNE = Color(0xFFFFD9C0)
    val NARANJA = Color(0xFFFF8A3D)
    val MORADA = Color(0xFF9E7DE8)
}

/** Medidas de cada forma de cuerpo: semianchos a tres alturas. */
private data class Silueta(
    val arriba: Float,
    val medio: Float,
    val abajo: Float,
    val cima: Float = Anatomia.CIMA,
    val base: Float = Anatomia.SUELO,
    val basePlana: Boolean = false
)

private val siluetas = listOf(
    Silueta(21f, 23f, 20f),                       // alubia (el original)
    Silueta(24f, 26.5f, 24f, cima = 22f),         // patata
    Silueta(16f, 22f, 24f),                       // huevo
    Silueta(14f, 19f, 27f),                       // pera
    Silueta(25f, 27f, 25f, cima = 28f),           // croqueta
    Silueta(14.5f, 15.5f, 14f, cima = 15f),       // churro
    Silueta(25f, 26f, 25f, cima = 23f, base = 71f), // bola
    Silueta(17f, 22f, 28f, basePlana = true)      // flan
)

/** Cuantas formas de cuerpo hay de verdad dibujadas. */
val TOTAL_SILUETAS = siluetas.size

/**
 * El contorno del cuerpo: un blob simetrico construido con cuatro curvas.
 * [aplastado] (0..1) lo achata un poco para la animacion de bailoteo.
 */
fun Pincel.caminoCuerpo(forma: Int, aplastado: Float = 0f): Camino {
    val s = siluetas[forma.mod(siluetas.size)]
    val cx = Anatomia.CX
    val cima = s.cima + aplastado * 3f
    val base = s.base
    val medio = (cima + base) / 2f
    // Al achatarse se ensancha, como un globo de agua.
    val ensanche = 1f + aplastado * 0.06f
    val wArriba = s.arriba * ensanche
    val wMedio = s.medio * ensanche
    val wAbajo = s.abajo * ensanche

    return camino {
        mueve(cx, cima)
        // Hombro derecho y bajada hasta la cintura.
        curva2(
            cx + wArriba * 1.05f, cima,
            cx + wMedio, cima + (medio - cima) * 0.45f,
            cx + wMedio, medio
        )
        if (s.basePlana) {
            // Falda de flan: se abre y apoya en una base recta.
            curva2(
                cx + wMedio, medio + (base - medio) * 0.6f,
                cx + wAbajo, base - 6f,
                cx + wAbajo, base - 1.5f
            )
            curva(cx + wAbajo, base, cx + wAbajo - 3f, base)
            recta(cx - wAbajo + 3f, base)
            curva(cx - wAbajo, base, cx - wAbajo, base - 1.5f)
            curva2(
                cx - wAbajo, base - 6f,
                cx - wMedio, medio + (base - medio) * 0.6f,
                cx - wMedio, medio
            )
        } else {
            // Culo redondeado.
            curva2(
                cx + wMedio, medio + (base - medio) * 0.55f,
                cx + wAbajo * 1.02f, base - (base - medio) * 0.12f,
                cx + wAbajo * 0.72f, base
            )
            curva2(
                cx + wAbajo * 0.3f, base + 2.6f,
                cx - wAbajo * 0.3f, base + 2.6f,
                cx - wAbajo * 0.72f, base
            )
            curva2(
                cx - wAbajo * 1.02f, base - (base - medio) * 0.12f,
                cx - wMedio, medio + (base - medio) * 0.55f,
                cx - wMedio, medio
            )
        }
        // Vuelta al hombro izquierdo.
        curva2(
            cx - wMedio, cima + (medio - cima) * 0.45f,
            cx - wArriba * 1.05f, cima,
            cx, cima
        )
        cierra()
    }
}

/**
 * Un miembro con pinta de tubo: se traza dos veces, primero gordo en negro y
 * luego mas fino en color piel. Sale un tubo con contorno perfecto y de grosor
 * uniforme, que es como estan hechos los brazos y las patas del monigote de
 * verdad (con paths cerrados salian palos de antena).
 *
 * Se dibujan ANTES del cuerpo, asi que el cuerpo tapa por donde se enganchan.
 */
private fun Pincel.tubo(relleno: Color, grosor: Float, trazado: Camino.() -> Unit) {
    val camino = camino(trazado)
    contornea(camino, tinta, grosor + 2.8f)
    contornea(camino, relleno, grosor)
}

/** Mano o pie: una bola con su contorno, del mismo grosor visual que el tubo. */
private fun Pincel.remate(x: Float, y: Float, radio: Float, relleno: Color) {
    circulo(x, y, radio + 1.4f, tinta)
    circulo(x, y, radio, relleno)
}

/**
 * Piernas y pies. [paso] (-1..1) mueve las patas para el bailoteo: en 0 estan
 * quietas, en los extremos una se cruza como en el monigote original.
 */
fun Pincel.dibujaPiernas(monigote: Monigote, paso: Float = 0f) {
    val piel = ColoresCuerpo.piel(monigote.color)
    val base = Anatomia.SUELO - 3f
    val pies = Anatomia.PIES_Y - 2f

    // Pierna izquierda: la que se cruza al bailar.
    val cruceIzq = 3.5f * paso
    tubo(piel, 7.5f) {
        mueve(43f, base)
        curva2(42f, base + 8f, 41f + cruceIzq, pies - 6f, 40.5f + cruceIzq * 1.6f, pies)
    }
    // El pie: un ovalo apuntando afuera.
    ovalo(36f + cruceIzq * 1.6f, pies + 1.5f, 6.5f, 4f, tinta)
    ovalo(36f + cruceIzq * 1.6f, pies + 1.2f, 5.2f, 2.9f, piel)

    // Pierna derecha: la que aguanta el peso.
    val cruceDer = -2.5f * paso
    tubo(piel, 7.5f) {
        mueve(57f, base)
        curva2(58f, base + 8f, 60f + cruceDer, pies - 6f, 61f + cruceDer, pies)
    }
    ovalo(65f + cruceDer, pies + 1.5f, 6.5f, 4f, tinta)
    ovalo(65f + cruceDer, pies + 1.2f, 5.2f, 2.9f, piel)
}

/**
 * Brazos: el izquierdo cae (y sujeta el cachivache) y el derecho va levantado
 * saludando, como el monigote del escritorio. [saludo] (0..1) lo agita.
 */
fun Pincel.dibujaBrazos(monigote: Monigote, saludo: Float = 0f) {
    val piel = ColoresCuerpo.piel(monigote.color)
    val manoDerY = Anatomia.MANO_DER_Y - saludo * 4f
    val manoDerX = Anatomia.MANO_DER_X + saludo * 2f

    // Brazo derecho, levantado en diagonal.
    tubo(piel, 7f) {
        mueve(67f, 46f)
        curva2(75f, 43f, 79f, 36f, manoDerX, manoDerY)
    }
    remate(manoDerX, manoDerY - 0.5f, 4.6f, piel)

    // Brazo izquierdo, colgando y separado del cuerpo.
    tubo(piel, 7f) {
        mueve(33f, 47f)
        curva2(25f, 51f, 20f, 57f, Anatomia.MANO_IZQ_X, Anatomia.MANO_IZQ_Y)
    }
    remate(Anatomia.MANO_IZQ_X - 0.5f, Anatomia.MANO_IZQ_Y + 1.5f, 4.6f, piel)
}

/** La sombrita del suelo, que asienta al bicho y no lo deja flotando. */
fun Pincel.dibujaSombraSuelo(alpha: Float = 0.16f) {
    ovalo(Anatomia.CX, Anatomia.PIES_Y + 3.5f, 24f, 4.2f, Tinta.NEGRA.copy(alpha = alpha))
}

/** Volumen del cuerpo: una franja de sombra a la izquierda y un brillo arriba. */
fun Pincel.dibujaVolumen(monigote: Monigote, forma: Int) {
    val sombra = ColoresCuerpo.sombra(monigote.color)
    val camino = caminoCuerpo(forma)
    // La sombra se recorta con la silueta para no salirse del cuerpo.
    val franja = camino {
        mueve(28f, 30f)
        curva2(24f, 45f, 26f, 62f, 34f, 74f)
        curva2(30f, 74f, 27f, 70f, 26f, 62f)
        curva2(25f, 50f, 26f, 38f, 28f, 30f)
        cierra()
    }
    rellena(franja, sombra.copy(alpha = 0.55f))
    // Brillo de plastico en la coronilla.
    ovalo(41f, 27f, 5.5f, 3.2f, Tinta.BLANCA.copy(alpha = 0.5f))
}
