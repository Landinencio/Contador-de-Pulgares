package com.pulgares.app.avatar

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Las piezas de la cara: ojos, bocas, gafas, pelambrera y detallitos. El indice
 * de cada funcion coincide con el catalogo de nombres en [Catalogos].
 */

private const val OJO_IZQ = Anatomia.CX - Anatomia.OJOS_SEP
private const val OJO_DER = Anatomia.CX + Anatomia.OJOS_SEP

/** Ojo del monigote original: ovalo negro gordo con su brillito. */
private fun Pincel.ojoBase(x: Float, rx: Float = 7f, ry: Float = 8f, y: Float = Anatomia.OJOS_Y) {
    ovalo(x, y, rx, ry, Tinta.NEGRA)
    ovalo(x + rx * 0.32f, y - ry * 0.38f, rx * 0.28f, ry * 0.26f, Tinta.BLANCA)
}

fun Pincel.dibujaOjos(indice: Int) {
    val y = Anatomia.OJOS_Y
    when (indice) {
        // Normales: los de la mascota.
        0 -> {
            ojoBase(OJO_IZQ)
            ojoBase(OJO_DER)
        }
        // Saltones: blanco con pupila que mira raro.
        1 -> {
            listOf(OJO_IZQ to -1f, OJO_DER to 1f).forEach { (x, lado) ->
                ovalo(x, y, 8.5f, 9f, Tinta.BLANCA)
                ovaloContorno(x, y, 8.5f, 9f, grosor = 2.8f)
                circulo(x + 2.2f * lado, y - 1f, 3.6f, Tinta.NEGRA)
            }
        }
        // Bizcos: las pupilas se miran entre si.
        2 -> {
            ovalo(OJO_IZQ, y, 8f, 8.5f, Tinta.BLANCA)
            ovaloContorno(OJO_IZQ, y, 8f, 8.5f, grosor = 2.8f)
            circulo(OJO_IZQ + 3.5f, y, 3.2f, Tinta.NEGRA)
            ovalo(OJO_DER, y, 8f, 8.5f, Tinta.BLANCA)
            ovaloContorno(OJO_DER, y, 8f, 8.5f, grosor = 2.8f)
            circulo(OJO_DER - 3.5f, y, 3.2f, Tinta.NEGRA)
        }
        // Dormido: dos arcos y unas pestanas.
        3 -> {
            arco(OJO_IZQ, y - 1f, 7f, 6f, 20f, 140f, grosor = 3f)
            arco(OJO_DER, y - 1f, 7f, 6f, 20f, 140f, grosor = 3f)
            linea(OJO_IZQ - 8f, y - 4f, OJO_IZQ - 10.5f, y - 6f, grosor = 2f)
            linea(OJO_DER + 8f, y - 4f, OJO_DER + 10.5f, y - 6f, grosor = 2f)
        }
        // Enamorado: corazones.
        4 -> {
            corazon(OJO_IZQ, y, 7.5f, Tinta.ROJA)
            corazon(OJO_DER, y, 7.5f, Tinta.ROJA)
        }
        // Signos de euro: el que solo piensa en la pasta.
        5 -> {
            euro(OJO_IZQ, y, 7f, Tinta.VERDE)
            euro(OJO_DER, y, 7f, Tinta.VERDE)
        }
        // Espiral: hipnotizado por la deuda.
        6 -> {
            listOf(OJO_IZQ, OJO_DER).forEach { x ->
                ovalo(x, y, 8f, 8.5f, Tinta.BLANCA)
                ovaloContorno(x, y, 8f, 8.5f, grosor = 2.6f)
                espiral(x, y, 6.5f)
            }
        }
        // Guino: uno abierto y otro cerrado.
        7 -> {
            ojoBase(OJO_IZQ)
            arco(OJO_DER, y, 7f, 5.5f, 190f, 160f, grosor = 3.2f)
        }
        // Enfadado: cejas gordas en pico.
        8 -> {
            ojoBase(OJO_IZQ, ry = 7f)
            ojoBase(OJO_DER, ry = 7f)
            linea(OJO_IZQ - 8f, y - 12f, OJO_IZQ + 6f, y - 8f, grosor = 3.6f)
            linea(OJO_DER + 8f, y - 12f, OJO_DER - 6f, y - 8f, grosor = 3.6f)
        }
        // Con ojeras: el que hizo las cuentas a las 4 de la manana.
        9 -> {
            ojoBase(OJO_IZQ, ry = 6.5f)
            ojoBase(OJO_DER, ry = 6.5f)
            arco(OJO_IZQ, y + 5f, 6.5f, 4f, 10f, 160f, color = Tinta.GRIS, grosor = 2.2f)
            arco(OJO_DER, y + 5f, 6.5f, 4f, 10f, 160f, color = Tinta.GRIS, grosor = 2.2f)
        }
        // Rayos laser: modo cobrador implacable.
        10 -> {
            listOf(OJO_IZQ, OJO_DER).forEach { x ->
                ovalo(x, y, 7f, 5f, Tinta.ROJA)
                ovaloContorno(x, y, 7f, 5f, grosor = 2.4f)
                ovalo(x, y, 3f, 2f, Tinta.BLANCA)
            }
            poligono(
                listOf(OJO_IZQ - 6f to y + 2f, OJO_IZQ - 2f to y + 30f, OJO_IZQ + 4f to y + 2f),
                Tinta.ROJA.copy(alpha = 0.45f),
                borde = Color.Transparent,
                grosor = 0f
            )
            poligono(
                listOf(OJO_DER - 4f to y + 2f, OJO_DER + 2f to y + 30f, OJO_DER + 6f to y + 2f),
                Tinta.ROJA.copy(alpha = 0.45f),
                borde = Color.Transparent,
                grosor = 0f
            )
        }
        // Estrellitas: le acaban de pagar.
        11 -> {
            estrella(OJO_IZQ, y, 8f, Tinta.AMARILLA)
            estrella(OJO_DER, y, 8f, Tinta.AMARILLA)
        }
        // Fulminado: dos cruces.
        12 -> {
            listOf(OJO_IZQ, OJO_DER).forEach { x ->
                linea(x - 5.5f, y - 5.5f, x + 5.5f, y + 5.5f, grosor = 3.4f)
                linea(x + 5.5f, y - 5.5f, x - 5.5f, y + 5.5f, grosor = 3.4f)
            }
        }
        // Robot: una visera con led.
        13 -> {
            caja(Anatomia.CX, y, 38f, 13f, 6f, Tinta.NEGRA)
            caja(Anatomia.CX, y, 38f, 13f, 6f, Tinta.NEGRA, soloContorno = true, grosor = 2.6f)
            circulo(OJO_IZQ, y, 3.2f, Tinta.ROJA)
            circulo(OJO_DER, y, 3.2f, Tinta.AZUL)
        }
        // Lloron: ojos cerrados y lagrimones.
        14 -> {
            arco(OJO_IZQ, y, 7f, 6f, 200f, 140f, grosor = 3.2f)
            arco(OJO_DER, y, 7f, 6f, 200f, 140f, grosor = 3.2f)
            listOf(OJO_IZQ, OJO_DER).forEach { x ->
                val lagrima = camino {
                    mueve(x, y + 3f)
                    curva(x - 4f, y + 10f, x, y + 15f)
                    curva(x + 4f, y + 10f, x, y + 3f)
                    cierra()
                }
                pieza(lagrima, Tinta.AZUL.copy(alpha = 0.85f), grosor = 2f)
            }
        }
        // Entrecerrados: mirada de "en serio?".
        15 -> {
            listOf(OJO_IZQ, OJO_DER).forEach { x ->
                ovalo(x, y, 7.5f, 4f, Tinta.BLANCA)
                ovaloContorno(x, y, 7.5f, 4f, grosor = 2.6f)
                circulo(x, y, 2.8f, Tinta.NEGRA)
            }
        }
    }
}

fun Pincel.dibujaBoca(indice: Int) {
    val y = Anatomia.BOCA_Y
    val cx = Anatomia.CX
    when (indice) {
        // Sonrisa sencilla.
        0 -> arco(cx, y - 3f, 9f, 7f, 20f, 140f, grosor = 3f)
        // Sonrisota abierta.
        1 -> {
            val cam = camino {
                mueve(cx - 10f, y - 3f)
                curva(cx, y + 11f, cx + 10f, y - 3f)
                curva(cx, y - 1f, cx - 10f, y - 3f)
                cierra()
            }
            pieza(cam, Tinta.NEGRA, grosor = 2.4f)
        }
        // Con dientes.
        2 -> {
            val cam = camino {
                mueve(cx - 10f, y - 3f)
                curva(cx, y + 10f, cx + 10f, y - 3f)
                cierra()
            }
            pieza(cam, Tinta.NEGRA, grosor = 2.4f)
            caja(cx, y - 1.5f, 15f, 3.6f, 1.2f, Tinta.BLANCA)
            linea(cx - 3.5f, y - 3.3f, cx - 3.5f, y + 0.3f, color = Tinta.GRIS, grosor = 1.2f)
            linea(cx + 3.5f, y - 3.3f, cx + 3.5f, y + 0.3f, color = Tinta.GRIS, grosor = 1.2f)
        }
        // Mueca de "esto me va a costar dinero".
        3 -> {
            val cam = camino {
                mueve(cx - 9f, y + 1f)
                curva(cx - 3f, y - 4f, cx, y + 1f)
                curva(cx + 4f, y + 5f, cx + 9f, y - 2f)
            }
            contornea(cam, grosor = 3f)
        }
        // Lengua fuera.
        4 -> {
            arco(cx, y - 3f, 9f, 7f, 20f, 140f, grosor = 3f)
            val lengua = camino {
                mueve(cx - 4f, y + 2.5f)
                curva(cx - 5f, y + 11f, cx + 1f, y + 10.5f)
                curva(cx + 5f, y + 10f, cx + 4f, y + 2.5f)
                cierra()
            }
            pieza(lengua, Tinta.ROSA, grosor = 2.2f)
        }
        // Silbando disimuladamente.
        5 -> {
            circulo(cx + 2f, y + 1f, 4.2f, Tinta.NEGRA)
            circulo(cx + 2f, y + 1f, 4.2f, Tinta.NEGRA)
            arco(cx - 9f, y - 4f, 5f, 4f, 200f, 120f, grosor = 2.2f)
        }
        // Colmillos de vampiro cobrador.
        6 -> {
            val cam = camino {
                mueve(cx - 10f, y - 3f)
                curva(cx, y + 9f, cx + 10f, y - 3f)
                cierra()
            }
            pieza(cam, Tinta.NEGRA, grosor = 2.4f)
            poligono(
                listOf(cx - 6f to y - 2f, cx - 3.5f to y - 2f, cx - 4.7f to y + 4.5f),
                Tinta.BLANCA, grosor = 1.4f
            )
            poligono(
                listOf(cx + 3.5f to y - 2f, cx + 6f to y - 2f, cx + 4.7f to y + 4.5f),
                Tinta.BLANCA, grosor = 1.4f
            )
        }
        // Grito de ver la cuenta.
        7 -> {
            ovalo(cx, y + 2f, 7f, 9f, Tinta.NEGRA)
            ovaloContorno(cx, y + 2f, 7f, 9f, grosor = 2.6f)
            ovalo(cx, y + 8f, 3.5f, 3f, Tinta.ROSA)
        }
        // Beso.
        8 -> {
            corazon(cx, y + 1f, 6f, Tinta.ROJA)
        }
        // Cremallera: aqui no se habla de dinero.
        9 -> {
            linea(cx - 11f, y, cx + 11f, y, grosor = 3f)
            (0..5).forEach { i ->
                val x = cx - 9f + i * 3.6f
                linea(x, y - 2.6f, x, y + 2.6f, grosor = 1.6f)
            }
            caja(cx + 12.5f, y, 5f, 6f, 1.5f, Tinta.GRIS)
            caja(cx + 12.5f, y, 5f, 6f, 1.5f, Tinta.NEGRA, soloContorno = true, grosor = 1.8f)
        }
        // Sin boca: el monigote original no tiene.
        10 -> Unit
        // Chupete: el benjamin del grupo.
        11 -> {
            circuloContorno(cx, y + 3f, 5.5f, grosor = 2.6f)
            circulo(cx, y + 3f, 5.5f, Tinta.ROSA)
            circuloContorno(cx, y + 3f, 5.5f, grosor = 2.6f)
            caja(cx, y - 2.5f, 7f, 4f, 2f, Tinta.AMARILLA)
            caja(cx, y - 2.5f, 7f, 4f, 2f, Tinta.NEGRA, soloContorno = true, grosor = 1.8f)
        }
        // Babeando por la comida de otro.
        12 -> {
            arco(cx, y - 3f, 9f, 7f, 20f, 140f, grosor = 3f)
            val baba = camino {
                mueve(cx + 5f, y + 3f)
                curva(cx + 4f, y + 12f, cx + 7f, y + 13f)
                curva(cx + 9f, y + 11f, cx + 8f, y + 3f)
                cierra()
            }
            pieza(baba, Tinta.AZUL.copy(alpha = 0.7f), grosor = 1.8f)
        }
        // Sonrisa torcida de listillo.
        13 -> {
            val cam = camino {
                mueve(cx - 9f, y + 2f)
                curva(cx, y + 8f, cx + 10f, y - 4f)
            }
            contornea(cam, grosor = 3.2f)
        }
        // Ohhh.
        14 -> {
            circulo(cx, y + 1f, 5.5f, Tinta.NEGRA)
            circulo(cx, y + 2.5f, 2.2f, Tinta.ROSA)
        }
        // Rechinando los dientes al ver su parte.
        15 -> {
            caja(cx, y, 20f, 9f, 2f, Tinta.NEGRA)
            caja(cx, y, 18f, 7f, 1.5f, Tinta.BLANCA)
            (0..4).forEach { i ->
                val x = cx - 7.2f + i * 3.6f
                linea(x, y - 3.5f, x, y + 3.5f, color = Tinta.GRIS, grosor = 1.2f)
            }
            linea(cx - 9f, y, cx + 9f, y, color = Tinta.GRIS, grosor = 1.2f)
        }
    }
}

fun Pincel.dibujaGafas(indice: Int) {
    val y = Anatomia.OJOS_Y
    when (indice) {
        0 -> Unit
        // De sol: cristales negros unidos.
        1 -> {
            caja(OJO_IZQ, y, 20f, 15f, 5f, Tinta.NEGRA)
            caja(OJO_DER, y, 20f, 15f, 5f, Tinta.NEGRA)
            linea(OJO_IZQ + 10f, y - 2f, OJO_DER - 10f, y - 2f, grosor = 2.6f)
            linea(OJO_IZQ - 10f, y - 1f, 24f, y - 3f, grosor = 2.4f)
            linea(OJO_DER + 10f, y - 1f, 76f, y - 3f, grosor = 2.4f)
            // Reflejo de gafa de pijo.
            linea(OJO_IZQ - 5f, y + 4f, OJO_IZQ + 2f, y - 4f, color = Tinta.BLANCA.copy(alpha = 0.5f), grosor = 2f)
        }
        // De pasta: montura gorda.
        2 -> {
            caja(OJO_IZQ, y, 19f, 16f, 5f, Color.Transparent, soloContorno = true, grosor = 3.4f)
            caja(OJO_DER, y, 19f, 16f, 5f, Color.Transparent, soloContorno = true, grosor = 3.4f)
            linea(OJO_IZQ + 9.5f, y, OJO_DER - 9.5f, y, grosor = 3f)
        }
        // Gafotas de culo de vaso.
        3 -> {
            listOf(OJO_IZQ, OJO_DER).forEach { x ->
                circulo(x, y, 10f, Tinta.BLANCA.copy(alpha = 0.35f))
                circuloContorno(x, y, 10f, grosor = 3.2f)
                circuloContorno(x, y, 6.5f, color = Tinta.BLANCA.copy(alpha = 0.6f), grosor = 1.6f)
            }
            linea(OJO_IZQ + 10f, y, OJO_DER - 10f, y, grosor = 2.8f)
        }
        // De esqui.
        4 -> {
            caja(Anatomia.CX, y, 46f, 18f, 9f, Tinta.AZUL.copy(alpha = 0.85f))
            caja(Anatomia.CX, y, 46f, 18f, 9f, Tinta.NEGRA, soloContorno = true, grosor = 3f)
            linea(28f, y - 4f, 42f, y - 7f, color = Tinta.BLANCA.copy(alpha = 0.55f), grosor = 3f)
        }
        // Antifaz de ladron de rondas.
        5 -> {
            val cam = camino {
                mueve(26f, y - 7f)
                curva(50f, y - 12f, 74f, y - 7f)
                curva(74f, y + 6f, 62f, y + 6f)
                curva(50f, y + 1f, 38f, y + 6f)
                curva(26f, y + 6f, 26f, y - 7f)
                cierra()
            }
            pieza(cam, Tinta.NEGRA, grosor = 2f)
        }
        // Monoculo de señorito.
        6 -> {
            circuloContorno(OJO_DER, y, 9.5f, grosor = 3f)
            circulo(OJO_DER, y, 9.5f, Tinta.BLANCA.copy(alpha = 0.28f))
            linea(OJO_DER + 8f, y + 6f, OJO_DER + 11f, y + 18f, grosor = 1.8f)
        }
        // Gafas de cine 3D.
        7 -> {
            caja(OJO_IZQ, y, 19f, 15f, 3f, Tinta.ROJA.copy(alpha = 0.75f))
            caja(OJO_DER, y, 19f, 15f, 3f, Tinta.AZUL.copy(alpha = 0.75f))
            caja(OJO_IZQ, y, 19f, 15f, 3f, Tinta.NEGRA, soloContorno = true, grosor = 2.6f)
            caja(OJO_DER, y, 19f, 15f, 3f, Tinta.NEGRA, soloContorno = true, grosor = 2.6f)
            linea(OJO_IZQ + 9.5f, y, OJO_DER - 9.5f, y, grosor = 2.6f)
        }
        // Nariz de Groucho, con bigote incluido.
        8 -> {
            caja(OJO_IZQ, y, 18f, 14f, 4f, Color.Transparent, soloContorno = true, grosor = 3.2f)
            caja(OJO_DER, y, 18f, 14f, 4f, Color.Transparent, soloContorno = true, grosor = 3.2f)
            linea(OJO_IZQ + 9f, y, OJO_DER - 9f, y, grosor = 2.8f)
            // Napia.
            val napia = camino {
                mueve(46f, y + 4f)
                curva2(43f, y + 13f, 50f, y + 17f, 54f, y + 11f)
                curva(55f, y + 6f, 54f, y + 4f)
                cierra()
            }
            pieza(napia, Tinta.CARNE, grosor = 2.2f)
            // Bigotazo.
            val bigote = camino {
                mueve(38f, y + 15f)
                curva(50f, y + 11f, 62f, y + 15f)
                curva(56f, y + 22f, 50f, y + 18f)
                curva(44f, y + 22f, 38f, y + 15f)
                cierra()
            }
            pieza(bigote, Tinta.NEGRA, grosor = 1.6f)
        }
        // De corazones.
        9 -> {
            corazon(OJO_IZQ, y, 9f, Tinta.ROSA.copy(alpha = 0.8f))
            corazon(OJO_DER, y, 9f, Tinta.ROSA.copy(alpha = 0.8f))
            linea(OJO_IZQ + 8f, y - 1f, OJO_DER - 8f, y - 1f, grosor = 2.4f)
        }
        // Deportivas de correr detras del moroso.
        10 -> {
            val cam = camino {
                mueve(27f, y - 5f)
                curva(50f, y - 11f, 73f, y - 5f)
                curva(73f, y + 5f, 50f, y + 3f)
                curva(27f, y + 5f, 27f, y - 5f)
                cierra()
            }
            pieza(cam, Tinta.NEGRA.copy(alpha = 0.82f), grosor = 2.6f)
            linea(30f, y - 3f, 44f, y - 6f, color = Tinta.AMARILLA, grosor = 2.4f)
        }
        // De soldador, para fundir la tarjeta.
        11 -> {
            caja(Anatomia.CX, y - 1f, 44f, 22f, 4f, Tinta.GRIS)
            caja(Anatomia.CX, y - 1f, 44f, 22f, 4f, Tinta.NEGRA, soloContorno = true, grosor = 3f)
            caja(Anatomia.CX, y - 1f, 30f, 11f, 2f, Tinta.NEGRA)
            linea(34f, y - 6f, 46f, y - 4f, color = Tinta.BLANCA.copy(alpha = 0.4f), grosor = 2f)
        }
    }
}

fun Pincel.dibujaBarba(indice: Int, monigote: Monigote) {
    val pelo = ColoresCuerpo.contraste(monigote.color)
    val y = Anatomia.BOCA_Y
    when (indice) {
        0 -> Unit
        // Perilla.
        1 -> {
            val cam = camino {
                mueve(45f, y + 6f)
                curva(50f, y + 9f, 55f, y + 6f)
                curva(54f, y + 15f, 50f, y + 16f)
                curva(46f, y + 15f, 45f, y + 6f)
                cierra()
            }
            pieza(cam, pelo, grosor = 1.8f)
        }
        // Bigote normal.
        2 -> {
            val cam = camino {
                mueve(40f, y - 6f)
                curva(50f, y - 10f, 60f, y - 6f)
                curva(55f, y - 1f, 50f, y - 4f)
                curva(45f, y - 1f, 40f, y - 6f)
                cierra()
            }
            pieza(cam, pelo, grosor = 1.6f)
        }
        // Bigoton de guardia civil.
        3 -> {
            val cam = camino {
                mueve(34f, y - 7f)
                curva(50f, y - 13f, 66f, y - 7f)
                curva(64f, y + 3f, 56f, y - 3f)
                curva(50f, y - 6f, 44f, y - 3f)
                curva(36f, y + 3f, 34f, y - 7f)
                cierra()
            }
            pieza(cam, pelo, grosor = 1.8f)
        }
        // Barba cerrada.
        4 -> {
            val cam = camino {
                mueve(30f, y - 8f)
                curva2(28f, y + 12f, 40f, y + 24f, 50f, y + 24f)
                curva2(60f, y + 24f, 72f, y + 12f, 70f, y - 8f)
                curva(62f, y - 2f, 58f, y + 2f)
                curva(50f, y + 6f, 42f, y + 2f)
                curva(38f, y - 2f, 30f, y - 8f)
                cierra()
            }
            pieza(cam, pelo, grosor = 2f)
        }
        // Chuletas.
        5 -> {
            listOf(29f, 71f).forEach { x ->
                val cam = camino {
                    mueve(x, y - 16f)
                    curva(x - 2f, y - 4f, x + if (x < 50f) 4f else -4f, y + 4f)
                    curva(x + if (x < 50f) 6f else -6f, y - 8f, x, y - 16f)
                    cierra()
                }
                pieza(cam, pelo, grosor = 1.8f)
            }
        }
        // De naufrago: le debe dinero a media isla.
        6 -> {
            val cam = camino {
                mueve(28f, y - 10f)
                curva2(24f, y + 18f, 40f, y + 34f, 50f, y + 33f)
                curva2(60f, y + 34f, 76f, y + 18f, 72f, y - 10f)
                curva(64f, y - 1f, 58f, y + 3f)
                curva(50f, y + 8f, 42f, y + 3f)
                curva(36f, y - 1f, 28f, y - 10f)
                cierra()
            }
            pieza(cam, pelo, grosor = 2f)
            // Greñas sueltas.
            linea(40f, y + 30f, 38f, y + 36f, color = pelo, grosor = 2.4f)
            linea(50f, y + 32f, 50f, y + 39f, color = pelo, grosor = 2.4f)
            linea(60f, y + 30f, 62f, y + 36f, color = pelo, grosor = 2.4f)
        }
        // Mosca bajo el labio.
        7 -> caja(Anatomia.CX, y + 8f, 6f, 5f, 1.5f, pelo)
        // Candado.
        8 -> {
            val cam = camino {
                mueve(36f, y - 6f)
                curva(50f, y - 11f, 64f, y - 6f)
                curva(60f, y - 2f, 54f, y - 3f)
                recta(55f, y + 14f)
                curva(50f, y + 18f, 45f, y + 14f)
                recta(46f, y - 3f)
                curva(40f, y - 2f, 36f, y - 6f)
                cierra()
            }
            pieza(cam, pelo, grosor = 1.8f)
        }
        // De vikingo, con trenzas.
        9 -> {
            val cam = camino {
                mueve(29f, y - 9f)
                curva2(26f, y + 14f, 40f, y + 28f, 50f, y + 28f)
                curva2(60f, y + 28f, 74f, y + 14f, 71f, y - 9f)
                curva(62f, y - 1f, 58f, y + 3f)
                curva(50f, y + 7f, 42f, y + 3f)
                curva(38f, y - 1f, 29f, y - 9f)
                cierra()
            }
            pieza(cam, pelo, grosor = 2f)
            listOf(40f, 60f).forEach { x ->
                (0..2).forEach { i ->
                    circulo(x, y + 26f + i * 4f, 2.6f, pelo)
                    circuloContorno(x, y + 26f + i * 4f, 2.6f, grosor = 1.4f)
                }
            }
        }
    }
}

fun Pincel.dibujaMarca(indice: Int) {
    val y = Anatomia.OJOS_Y
    when (indice) {
        0 -> Unit
        // Sonrojo.
        1 -> {
            ovalo(31f, y + 11f, 6f, 3.6f, Tinta.ROSA.copy(alpha = 0.6f))
            ovalo(69f, y + 11f, 6f, 3.6f, Tinta.ROSA.copy(alpha = 0.6f))
        }
        // Pecas.
        2 -> {
            listOf(
                32f to y + 9f, 36f to y + 12f, 30f to y + 14f,
                68f to y + 9f, 64f to y + 12f, 70f to y + 14f
            ).forEach { (x, py) -> circulo(x, py, 1.3f, Tinta.MARRON) }
        }
        // Ojeras marcadas.
        3 -> {
            arco(OJO_IZQ, y + 7f, 7f, 4.5f, 10f, 160f, color = Tinta.GRIS, grosor = 2.4f)
            arco(OJO_DER, y + 7f, 7f, 4.5f, 10f, 160f, color = Tinta.GRIS, grosor = 2.4f)
        }
        // Tirita en la frente.
        4 -> {
            girado(-20f, 36f, 27f) {
                caja(36f, 27f, 15f, 6f, 2f, Tinta.CARNE)
                caja(36f, 27f, 15f, 6f, 2f, Tinta.NEGRA, soloContorno = true, grosor = 1.8f)
                caja(36f, 27f, 6f, 6f, 1f, Tinta.CARNE.copy(alpha = 0.6f))
            }
        }
        // Gota de sudor: acaba de ver el total.
        5 -> {
            val gota = camino {
                mueve(72f, 26f)
                curva(67f, 33f, 72f, 37f)
                curva(77f, 33f, 72f, 26f)
                cierra()
            }
            pieza(gota, Tinta.AZUL.copy(alpha = 0.8f), grosor = 1.8f)
        }
        // Lagrimon.
        6 -> {
            val lag = camino {
                mueve(OJO_DER + 5f, y + 4f)
                curva(OJO_DER + 1f, y + 14f, OJO_DER + 5f, y + 19f)
                curva(OJO_DER + 9f, y + 14f, OJO_DER + 5f, y + 4f)
                cierra()
            }
            pieza(lag, Tinta.AZUL.copy(alpha = 0.85f), grosor = 1.8f)
        }
        // Chichon con estrellitas.
        7 -> {
            circulo(63f, 24f, 5f, Tinta.ROJA.copy(alpha = 0.8f))
            circuloContorno(63f, 24f, 5f, grosor = 2f)
            estrella(70f, 18f, 3.5f, Tinta.AMARILLA)
            estrella(56f, 17f, 2.8f, Tinta.AMARILLA)
        }
        // Granos de la mala vida.
        8 -> {
            listOf(33f to y + 10f, 67f to y + 13f, 44f to y + 18f).forEach { (x, py) ->
                circulo(x, py, 2.2f, Tinta.ROJA.copy(alpha = 0.75f))
                circulo(x, py - 0.5f, 0.9f, Tinta.BLANCA)
            }
        }
        // Tatuaje de corazon.
        9 -> corazon(68f, Anatomia.BOCA_Y + 12f, 5f, Tinta.ROJA.copy(alpha = 0.8f))
        // Cicatriz de guerra (de una discusion por la cuenta).
        10 -> {
            linea(66f, 24f, 72f, 34f, grosor = 2.2f)
            linea(64f, 27f, 70f, 29f, grosor = 1.8f)
            linea(67f, 31f, 73f, 30f, grosor = 1.8f)
        }
        // Purpurina de after.
        11 -> {
            listOf(
                34f to 28f, 42f to 24f, 58f to 26f, 66f to 30f,
                38f to y + 16f, 62f to y + 18f, 50f to 23f
            ).forEachIndexed { i, (x, py) ->
                val color = if (i % 2 == 0) Tinta.AMARILLA else Tinta.ROSA
                estrella(x, py, 2.6f, color)
            }
        }
    }
}

// ---- figuritas reutilizables ----

fun Pincel.corazon(x: Float, y: Float, tamano: Float, color: Color) {
    val cam = camino {
        mueve(x, y + tamano * 0.85f)
        curva2(
            x - tamano * 1.35f, y + tamano * 0.05f,
            x - tamano * 0.7f, y - tamano * 1.05f,
            x, y - tamano * 0.28f
        )
        curva2(
            x + tamano * 0.7f, y - tamano * 1.05f,
            x + tamano * 1.35f, y + tamano * 0.05f,
            x, y + tamano * 0.85f
        )
        cierra()
    }
    pieza(cam, color, grosor = 1.8f)
}

fun Pincel.estrella(x: Float, y: Float, tamano: Float, color: Color) {
    val puntos = mutableListOf<Pair<Float, Float>>()
    for (i in 0 until 10) {
        val radio = if (i % 2 == 0) tamano else tamano * 0.45f
        val angulo = Math.toRadians((i * 36 - 90).toDouble())
        puntos += (x + radio * cos(angulo).toFloat()) to (y + radio * sin(angulo).toFloat())
    }
    poligono(puntos, color, grosor = 1.4f)
}

/** El simbolo del euro, dibujado a mano: una C con dos rayitas. */
fun Pincel.euro(x: Float, y: Float, tamano: Float, color: Color) {
    arco(x + tamano * 0.15f, y, tamano * 0.85f, tamano, 40f, 280f, color = color, grosor = tamano * 0.32f)
    linea(x - tamano * 0.9f, y - tamano * 0.28f, x + tamano * 0.55f, y - tamano * 0.28f, color = color, grosor = tamano * 0.26f)
    linea(x - tamano * 0.9f, y + tamano * 0.28f, x + tamano * 0.55f, y + tamano * 0.28f, color = color, grosor = tamano * 0.26f)
}

/** Espiral de hipnotizado, hecha a trocitos de arco. */
fun Pincel.espiral(x: Float, y: Float, radio: Float) {
    var r = radio
    var angulo = 0f
    while (r > 0.9f) {
        arco(x, y, r, r, angulo, 170f, grosor = 1.6f)
        angulo += 170f
        r -= radio * 0.17f
    }
}
