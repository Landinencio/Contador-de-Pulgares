package com.pulgares.app.avatar

import androidx.compose.ui.graphics.Color

/**
 * Lo que el monigote lleva en la mano izquierda (la que cuelga) y los fondos.
 * El cachivache se dibuja alrededor de (17, 70) para que parezca agarrado.
 */

private const val MX = 17f   // mano
private const val MY = 71f

fun Pincel.dibujaCachivache(indice: Int) {
    when (indice) {
        0 -> Unit
        // Jarra de birra con espuma.
        1 -> {
            caja(MX, MY, 15f, 18f, 3f, Tinta.AMARILLA)
            caja(MX, MY, 15f, 18f, 3f, Tinta.NEGRA, soloContorno = true, grosor = 2.4f)
            // Espuma.
            listOf(MX - 4f to MY - 9f, MX to MY - 11f, MX + 4f to MY - 9f).forEach { (x, y) ->
                circulo(x, y, 3.6f, Tinta.BLANCA)
                circuloContorno(x, y, 3.6f, grosor = 1.6f)
            }
            // Asa.
            arco(MX + 9f, MY + 1f, 5f, 5f, 300f, 120f, grosor = 2.4f)
        }
        // Churro (con su bolsa de papel).
        2 -> {
            girado(-18f, MX, MY) {
                caja(MX, MY + 2f, 7f, 22f, 3.5f, Tinta.MARRON)
                caja(MX, MY + 2f, 7f, 22f, 3.5f, Tinta.NEGRA, soloContorno = true, grosor = 2.2f)
                // Las estrias del churro.
                linea(MX - 1.5f, MY - 6f, MX - 1.5f, MY + 10f, color = Tinta.NEGRA.copy(alpha = 0.35f), grosor = 1.2f)
                linea(MX + 1.5f, MY - 6f, MX + 1.5f, MY + 10f, color = Tinta.NEGRA.copy(alpha = 0.35f), grosor = 1.2f)
            }
        }
        // Movil con la app abierta.
        3 -> {
            caja(MX, MY, 13f, 21f, 3f, Tinta.NEGRA)
            caja(MX, MY, 10f, 17f, 1.5f, Tinta.AZUL.copy(alpha = 0.85f))
            // El monigote dentro del movil, muy meta.
            circulo(MX, MY - 3f, 2.4f, Tinta.ROSA)
            caja(MX, MY + 4f, 6f, 2f, 1f, Tinta.BLANCA)
        }
        // Billete que se agita.
        4 -> {
            girado(-12f, MX, MY) {
                caja(MX, MY, 24f, 13f, 2f, Tinta.VERDE.copy(alpha = 0.9f))
                caja(MX, MY, 24f, 13f, 2f, Tinta.NEGRA, soloContorno = true, grosor = 2.2f)
                circuloContorno(MX, MY, 4f, color = Tinta.NEGRA.copy(alpha = 0.55f), grosor = 1.4f)
                euro(MX, MY, 2.6f, Tinta.NEGRA.copy(alpha = 0.65f))
            }
        }
        // Tarjeta de credito.
        5 -> {
            girado(-15f, MX, MY) {
                caja(MX, MY, 25f, 16f, 2.5f, Tinta.MORADA)
                caja(MX, MY, 25f, 16f, 2.5f, Tinta.NEGRA, soloContorno = true, grosor = 2.2f)
                caja(MX - 6f, MY - 3f, 6f, 4.5f, 1f, Tinta.DORADA)
                linea(MX - 10f, MY + 4f, MX + 8f, MY + 4f, color = Tinta.BLANCA.copy(alpha = 0.8f), grosor = 1.6f)
            }
        }
        // Calculadora del que revisa la cuenta tres veces.
        6 -> {
            caja(MX, MY, 17f, 22f, 2.5f, Tinta.GRIS)
            caja(MX, MY, 17f, 22f, 2.5f, Tinta.NEGRA, soloContorno = true, grosor = 2.2f)
            caja(MX, MY - 6.5f, 12f, 5f, 1f, Tinta.VERDE.copy(alpha = 0.8f))
            (0..2).forEach { fila ->
                (0..2).forEach { col ->
                    circulo(MX - 4f + col * 4f, MY + 1f + fila * 4.5f, 1.4f, Tinta.NEGRA)
                }
            }
        }
        // Bocadillo de calamares.
        7 -> {
            girado(-10f, MX, MY) {
                val pan = camino {
                    mueve(MX - 12f, MY + 4f)
                    curva2(MX - 14f, MY - 6f, MX + 14f, MY - 6f, MX + 12f, MY + 4f)
                    curva2(MX + 6f, MY + 8f, MX - 6f, MY + 8f, MX - 12f, MY + 4f)
                    cierra()
                }
                pieza(pan, Tinta.AMARILLA, grosor = 2.2f)
                linea(MX - 9f, MY, MX + 9f, MY, color = Tinta.MARRON, grosor = 2.6f)
            }
        }
        // Dado: quien paga se juega a los dados.
        8 -> {
            caja(MX, MY, 18f, 18f, 4f, Tinta.BLANCA)
            caja(MX, MY, 18f, 18f, 4f, Tinta.NEGRA, soloContorno = true, grosor = 2.4f)
            listOf(
                MX - 4.5f to MY - 4.5f, MX + 4.5f to MY - 4.5f,
                MX to MY, MX - 4.5f to MY + 4.5f, MX + 4.5f to MY + 4.5f
            ).forEach { (x, y) -> circulo(x, y, 1.8f, Tinta.NEGRA) }
        }
        // Pulgar gigante de espuma: el logo de la app.
        9 -> {
            girado(-12f, MX, MY) {
                val mano = camino {
                    mueve(MX - 8f, MY + 10f)
                    recta(MX - 8f, MY - 2f)
                    curva2(MX - 9f, MY - 12f, MX - 1f, MY - 14f, MX - 1f, MY - 5f)
                    curva(MX + 9f, MY - 6f, MX + 8f, MY + 2f)
                    recta(MX + 8f, MY + 10f)
                    cierra()
                }
                pieza(mano, Tinta.AMARILLA, grosor = 2.4f)
            }
        }
        // Cartera vacia con polillas.
        10 -> {
            val cartera = camino {
                mueve(MX - 12f, MY - 4f)
                recta(MX + 12f, MY - 6f)
                curva(MX + 14f, MY + 6f, MX + 11f, MY + 7f)
                recta(MX - 11f, MY + 8f)
                cierra()
            }
            pieza(cartera, Tinta.MARRON, grosor = 2.4f)
            // Nada dentro y una polilla saliendo.
            linea(MX - 6f, MY - 5f, MX + 6f, MY - 6f, color = Tinta.NEGRA.copy(alpha = 0.4f), grosor = 1.6f)
            circulo(MX + 4f, MY - 14f, 2.2f, Tinta.GRIS)
            linea(MX + 2f, MY - 16f, MX + 6f, MY - 12f, color = Tinta.GRIS, grosor = 1.4f)
            linea(MX + 6f, MY - 16f, MX + 2f, MY - 12f, color = Tinta.GRIS, grosor = 1.4f)
        }
        // Litrona de botellon.
        11 -> {
            val botella = camino {
                mueve(MX - 6f, MY + 12f)
                recta(MX - 6f, MY - 4f)
                curva(MX - 5f, MY - 10f, MX - 2.5f, MY - 12f)
                recta(MX - 2.5f, MY - 18f)
                recta(MX + 2.5f, MY - 18f)
                recta(MX + 2.5f, MY - 12f)
                curva(MX + 5f, MY - 10f, MX + 6f, MY - 4f)
                recta(MX + 6f, MY + 12f)
                cierra()
            }
            pieza(botella, Tinta.VERDE.copy(alpha = 0.75f), grosor = 2.2f)
            caja(MX, MY - 19f, 6f, 3f, 1f, Tinta.DORADA)
            caja(MX, MY + 3f, 12f, 7f, 1f, Tinta.BLANCA.copy(alpha = 0.85f))
        }
        // Paraguas, porque siempre llueve sobre el que paga.
        12 -> {
            arco(MX, MY - 4f, 14f, 12f, 180f, 180f, color = Tinta.ROJA, grosor = 6f)
            linea(MX, MY - 4f, MX, MY + 14f, grosor = 2.4f)
            arco(MX - 3f, MY + 14f, 3f, 3f, 0f, 180f, grosor = 2.4f)
        }
        // Porcion de pizza.
        13 -> {
            girado(-15f, MX, MY) {
                val porcion = camino {
                    mueve(MX, MY - 12f)
                    recta(MX + 11f, MY + 10f)
                    curva(MX, MY + 14f, MX - 11f, MY + 10f)
                    cierra()
                }
                pieza(porcion, Tinta.AMARILLA, grosor = 2.4f)
                caja(MX, MY + 10f, 21f, 5f, 2f, Tinta.MARRON.copy(alpha = 0.85f))
                circulo(MX - 3f, MY + 2f, 2.2f, Tinta.ROJA)
                circulo(MX + 4f, MY + 6f, 2.2f, Tinta.ROJA)
                circulo(MX + 1f, MY - 4f, 1.8f, Tinta.ROJA)
            }
        }
        // Mando de la tele: el que no se levanta ni para pagar.
        14 -> {
            caja(MX, MY, 12f, 24f, 3f, Tinta.NEGRA)
            caja(MX, MY - 8f, 8f, 4f, 1f, Tinta.ROJA)
            (0..2).forEach { fila ->
                (0..1).forEach { col ->
                    circulo(MX - 2.5f + col * 5f, MY + fila * 4.5f, 1.4f, Tinta.GRIS)
                }
            }
        }
        // Pancarta reivindicativa.
        15 -> {
            linea(MX, MY + 14f, MX, MY - 14f, color = Tinta.MARRON, grosor = 2.6f)
            caja(MX + 2f, MY - 12f, 30f, 16f, 2f, Tinta.BLANCA)
            caja(MX + 2f, MY - 12f, 30f, 16f, 2f, Tinta.NEGRA, soloContorno = true, grosor = 2.2f)
            // "€" tachado: no pago.
            euro(MX - 3f, MY - 12f, 4f, Tinta.NEGRA)
            linea(MX - 9f, MY - 6f, MX + 3f, MY - 18f, color = Tinta.ROJA, grosor = 2.4f)
            linea(MX + 8f, MY - 15f, MX + 15f, MY - 15f, color = Tinta.NEGRA, grosor = 1.8f)
            linea(MX + 8f, MY - 10f, MX + 15f, MY - 10f, color = Tinta.NEGRA, grosor = 1.8f)
        }
        // El maletín del Cobrador del Frac. Dentro solo lleva paciencia.
        16 -> {
            arco(MX, MY - 6f, 5f, 4.5f, 180f, 180f, grosor = 2.6f)
            caja(MX, MY + 1f, 22f, 15f, 3f, Tinta.MARRON)
            caja(MX, MY + 1f, 22f, 15f, 3f, Tinta.NEGRA, soloContorno = true, grosor = 2.4f)
            // La junta de la tapa y el cierre dorado.
            linea(MX - 11f, MY - 1.5f, MX + 11f, MY - 1.5f, color = Tinta.NEGRA.copy(alpha = 0.45f), grosor = 1.6f)
            caja(MX, MY + 0.5f, 4.5f, 3.6f, 1f, Tinta.DORADA)
            caja(MX, MY + 0.5f, 4.5f, 3.6f, 1f, Tinta.NEGRA, soloContorno = true, grosor = 1.4f)
        }
    }
}

/** Los 14 fondos del avatar. Se dibujan antes que el bicho, obviamente. */
fun Pincel.dibujaFondo(indice: Int) {
    val base = when (indice) {
        0 -> Color(0xFFFFD9E6)
        1 -> Color(0xFFF7EEDD)
        2 -> Color(0xFFD6EBFF)
        3 -> Color(0xFFD8F3DE)
        4 -> Color(0xFFFFE9C7)
        5 -> Color(0xFFE7DDFF)
        6 -> Color(0xFF2B2540)
        7 -> Color(0xFFFFF3CC)
        8 -> Color(0xFFD9F5EF)
        9 -> Color(0xFFF7F1E4)
        10 -> Color(0xFF1F2B45)
        11 -> Color(0xFFFFCBA8)
        12 -> Color(0xFFEDEDED)
        else -> Color(0xFFFFE0EC)
    }
    caja(50f, 50f, 200f, 200f, 0f, base)

    val trazo = if (indice == 6 || indice == 10) {
        Tinta.BLANCA.copy(alpha = 0.35f)
    } else {
        Tinta.NEGRA.copy(alpha = 0.12f)
    }

    when (indice) {
        // Lunares.
        4 -> for (fila in 0..5) for (col in 0..5) {
            val desfase = if (fila % 2 == 0) 0f else 9f
            circulo(col * 18f + desfase, fila * 18f + 4f, 3.4f, trazo)
        }
        // Rayas diagonales.
        5 -> for (i in -3..10) {
            linea(i * 14f, 0f, i * 14f + 60f, 100f, color = trazo, grosor = 5f)
        }
        // Estrellas de noche.
        6 -> {
            listOf(
                12f to 15f, 30f to 8f, 48f to 20f, 70f to 10f, 88f to 22f,
                20f to 40f, 82f to 45f, 8f to 62f, 92f to 70f, 16f to 88f, 60f to 92f
            ).forEachIndexed { i, (x, y) ->
                estrella(x, y, if (i % 3 == 0) 3.4f else 2.2f, Tinta.AMARILLA.copy(alpha = 0.85f))
            }
        }
        // Monedas cayendo.
        7 -> listOf(
            10f to 18f, 26f to 8f, 44f to 24f, 66f to 12f, 86f to 20f,
            14f to 46f, 88f to 52f, 6f to 74f, 94f to 80f, 22f to 90f
        ).forEach { (x, y) ->
            circulo(x, y, 5f, Tinta.DORADA.copy(alpha = 0.65f))
            circuloContorno(x, y, 5f, color = Tinta.NEGRA.copy(alpha = 0.2f), grosor = 1.4f)
            euro(x, y, 2.4f, Tinta.NEGRA.copy(alpha = 0.25f))
        }
        // Billetes volando.
        8 -> listOf(
            Triple(12f, 20f, -15f), Triple(74f, 14f, 12f), Triple(20f, 78f, 20f),
            Triple(84f, 62f, -18f), Triple(50f, 8f, 8f)
        ).forEach { (x, y, giro) ->
            girado(giro, x, y) {
                caja(x, y, 22f, 12f, 2f, Tinta.VERDE.copy(alpha = 0.45f))
                caja(x, y, 22f, 12f, 2f, trazo, soloContorno = true, grosor = 1.6f)
            }
        }
        // Garabatos varios.
        9 -> {
            arco(16f, 18f, 8f, 8f, 200f, 220f, color = trazo, grosor = 2.6f)
            estrella(80f, 16f, 5f, trazo)
            corazon(88f, 76f, 5f, trazo)
            circuloContorno(14f, 80f, 6f, color = trazo, grosor = 2.6f)
            linea(64f, 88f, 76f, 92f, color = trazo, grosor = 2.6f)
            euro(50f, 12f, 5f, trazo)
        }
        // Rayos de superheroe del bizum.
        10 -> for (i in 0..11) {
            val angulo = i * 30f
            girado(angulo, 50f, 50f) {
                poligono(
                    listOf(50f to 50f, 44f to -10f, 56f to -10f),
                    Tinta.AMARILLA.copy(alpha = 0.18f),
                    borde = Color.Transparent,
                    grosor = 0f
                )
            }
        }
        // Atardecer a franjas.
        11 -> {
            caja(50f, 20f, 200f, 40f, 0f, Color(0xFFFFB088))
            caja(50f, 52f, 200f, 26f, 0f, Color(0xFFFF8FA8))
            caja(50f, 78f, 200f, 30f, 0f, Color(0xFFFFC9A0))
            circulo(50f, 46f, 16f, Color(0xFFFFE08A))
        }
        // Cuadricula de cuaderno.
        12 -> {
            for (i in 0..9) {
                linea(i * 11f, 0f, i * 11f, 100f, color = trazo, grosor = 1.4f)
                linea(0f, i * 11f, 100f, i * 11f, color = trazo, grosor = 1.4f)
            }
        }
        // Confeti de fiesta.
        13 -> listOf(
            10f to 14f, 28f to 6f, 46f to 18f, 68f to 8f, 88f to 16f,
            18f to 38f, 80f to 42f, 6f to 60f, 94f to 66f, 24f to 84f,
            58f to 90f, 40f to 70f
        ).forEachIndexed { i, (x, y) ->
            val color = listOf(
                Tinta.ROSA, Tinta.AMARILLA, Tinta.AZUL, Tinta.VERDE, Tinta.MORADA
            )[i % 5].copy(alpha = 0.75f)
            girado((i * 37).toFloat(), x, y) {
                caja(x, y, 7f, 3.4f, 1.4f, color)
            }
        }
    }
}
