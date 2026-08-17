package com.pulgares.app.avatar

import androidx.compose.ui.graphics.Color

/**
 * Lo que le crece o le ponen en la cabeza. La coronilla esta en
 * [Anatomia.CIMA] (y=20) y a esa altura el cuerpo mide unos 42 de ancho.
 */

private const val CIMA = Anatomia.CIMA
private const val CX = Anatomia.CX

fun Pincel.dibujaPelo(indice: Int, monigote: Monigote) {
    val pelo = ColoresCuerpo.contraste(monigote.color)
    when (indice) {
        0 -> Unit
        // Tres pelos de toda la vida.
        1 -> {
            linea(CX - 4f, CIMA + 1f, CX - 6f, CIMA - 8f, color = pelo, grosor = 2.4f)
            linea(CX, CIMA - 0.5f, CX, CIMA - 10f, color = pelo, grosor = 2.4f)
            linea(CX + 4f, CIMA + 1f, CX + 6f, CIMA - 8f, color = pelo, grosor = 2.4f)
        }
        // Tupe de galan.
        2 -> {
            val cam = camino {
                mueve(CX - 20f, CIMA + 6f)
                curva2(CX - 18f, CIMA - 8f, CX + 4f, CIMA - 14f, CX + 16f, CIMA - 6f)
                curva2(CX + 10f, CIMA - 4f, CX + 4f, CIMA - 2f, CX + 20f, CIMA + 5f)
                curva2(CX + 6f, CIMA - 1f, CX - 8f, CIMA - 1f, CX - 20f, CIMA + 6f)
                cierra()
            }
            pieza(cam, pelo, grosor = 2f)
        }
        // Afro.
        3 -> {
            listOf(
                CX - 16f to CIMA - 4f, CX - 8f to CIMA - 11f, CX + 2f to CIMA - 13f,
                CX + 12f to CIMA - 9f, CX + 18f to CIMA - 1f, CX - 20f to CIMA + 4f,
                CX + 21f to CIMA + 6f
            ).forEach { (x, y) ->
                circulo(x, y, 9f, pelo)
            }
            // Contorno exterior aproximado para que no parezca sucio.
            listOf(
                CX - 16f to CIMA - 4f, CX - 8f to CIMA - 11f, CX + 2f to CIMA - 13f,
                CX + 12f to CIMA - 9f, CX + 18f to CIMA - 1f
            ).forEach { (x, y) -> circuloContorno(x, y, 9f, grosor = 1.6f) }
        }
        // Cresta punk.
        4 -> {
            val cam = camino {
                mueve(CX - 14f, CIMA + 4f)
                recta(CX - 9f, CIMA - 14f)
                recta(CX - 5f, CIMA - 2f)
                recta(CX - 1f, CIMA - 18f)
                recta(CX + 4f, CIMA - 2f)
                recta(CX + 8f, CIMA - 13f)
                recta(CX + 13f, CIMA + 4f)
                cierra()
            }
            pieza(cam, Tinta.ROSA, grosor = 2f)
        }
        // Mono en lo alto.
        5 -> {
            circulo(CX, CIMA - 9f, 8f, pelo)
            circuloContorno(CX, CIMA - 9f, 8f, grosor = 2.2f)
            val base = camino {
                mueve(CX - 18f, CIMA + 5f)
                curva2(CX - 14f, CIMA - 6f, CX + 14f, CIMA - 6f, CX + 18f, CIMA + 5f)
                curva2(CX + 8f, CIMA + 1f, CX - 8f, CIMA + 1f, CX - 18f, CIMA + 5f)
                cierra()
            }
            pieza(base, pelo, grosor = 2f)
        }
        // Coleta a un lado.
        6 -> {
            val base = camino {
                mueve(CX - 19f, CIMA + 6f)
                curva2(CX - 16f, CIMA - 8f, CX + 16f, CIMA - 8f, CX + 19f, CIMA + 6f)
                curva2(CX + 8f, CIMA + 2f, CX - 8f, CIMA + 2f, CX - 19f, CIMA + 6f)
                cierra()
            }
            pieza(base, pelo, grosor = 2f)
            val coleta = camino {
                mueve(CX + 17f, CIMA + 2f)
                curva2(CX + 30f, CIMA + 4f, CX + 32f, CIMA + 18f, CX + 26f, CIMA + 24f)
                curva2(CX + 24f, CIMA + 14f, CX + 22f, CIMA + 8f, CX + 15f, CIMA + 8f)
                cierra()
            }
            pieza(coleta, pelo, grosor = 2f)
            circulo(CX + 18f, CIMA + 4f, 2.6f, Tinta.ROSA)
        }
        // Rizos.
        7 -> {
            listOf(
                CX - 17f to CIMA + 1f, CX - 10f to CIMA - 6f, CX - 2f to CIMA - 9f,
                CX + 7f to CIMA - 6f, CX + 15f to CIMA + 1f
            ).forEach { (x, y) ->
                circulo(x, y, 7f, pelo)
                circuloContorno(x, y, 7f, grosor = 1.8f)
            }
            listOf(CX - 20f to CIMA + 8f, CX + 19f to CIMA + 8f).forEach { (x, y) ->
                circulo(x, y, 5.5f, pelo)
                circuloContorno(x, y, 5.5f, grosor = 1.6f)
            }
        }
        // Flequillo recto de personaje serio.
        8 -> {
            val cam = camino {
                mueve(CX - 20f, CIMA + 8f)
                curva2(CX - 20f, CIMA - 8f, CX + 20f, CIMA - 8f, CX + 20f, CIMA + 8f)
                recta(CX + 18f, CIMA + 9f)
                curva2(CX + 10f, CIMA + 4f, CX - 10f, CIMA + 4f, CX - 18f, CIMA + 9f)
                cierra()
            }
            pieza(cam, pelo, grosor = 2f)
        }
        // Mullet: negocios delante, fiesta detras.
        9 -> {
            val cam = camino {
                mueve(CX - 20f, CIMA + 7f)
                curva2(CX - 18f, CIMA - 9f, CX + 18f, CIMA - 9f, CX + 20f, CIMA + 7f)
                curva2(CX + 26f, CIMA + 22f, CX + 22f, CIMA + 30f, CX + 16f, CIMA + 30f)
                curva2(CX + 18f, CIMA + 18f, CX + 14f, CIMA + 10f, CX, CIMA + 8f)
                curva2(CX - 14f, CIMA + 10f, CX - 18f, CIMA + 18f, CX - 16f, CIMA + 30f)
                curva2(CX - 22f, CIMA + 30f, CX - 26f, CIMA + 22f, CX - 20f, CIMA + 7f)
                cierra()
            }
            pieza(cam, pelo, grosor = 2f)
        }
        // Rastas.
        10 -> {
            val base = camino {
                mueve(CX - 20f, CIMA + 6f)
                curva2(CX - 17f, CIMA - 9f, CX + 17f, CIMA - 9f, CX + 20f, CIMA + 6f)
                curva2(CX + 8f, CIMA + 2f, CX - 8f, CIMA + 2f, CX - 20f, CIMA + 6f)
                cierra()
            }
            pieza(base, pelo, grosor = 2f)
            listOf(-19f, -13f, 13f, 19f).forEachIndexed { i, dx ->
                val largo = if (i % 2 == 0) 26f else 20f
                caja(CX + dx, CIMA + 4f + largo / 2f, 5f, largo, 2.5f, pelo)
                circulo(CX + dx, CIMA + 4f + largo, 3f, Tinta.AMARILLA)
                circuloContorno(CX + dx, CIMA + 4f + largo, 3f, grosor = 1.4f)
            }
        }
        // Calva brillante: sin pelo pero con destello.
        11 -> {
            ovalo(CX - 5f, CIMA + 5f, 7f, 3.4f, Tinta.BLANCA.copy(alpha = 0.75f))
            linea(CX + 8f, CIMA + 2f, CX + 13f, CIMA - 2f, color = Tinta.BLANCA, grosor = 2f)
            linea(CX + 11f, CIMA + 5f, CX + 16f, CIMA + 4f, color = Tinta.BLANCA, grosor = 2f)
        }
        // Con entradas: la calva avanzando.
        12 -> {
            val cam = camino {
                mueve(CX - 20f, CIMA + 10f)
                curva2(CX - 20f, CIMA - 2f, CX - 12f, CIMA + 1f, CX - 8f, CIMA + 6f)
                curva2(CX, CIMA - 2f, CX + 8f, CIMA + 6f, CX + 8f, CIMA + 6f)
                curva2(CX + 12f, CIMA + 1f, CX + 20f, CIMA - 2f, CX + 20f, CIMA + 10f)
                curva2(CX + 10f, CIMA + 6f, CX - 10f, CIMA + 6f, CX - 20f, CIMA + 10f)
                cierra()
            }
            pieza(cam, pelo, grosor = 2f)
        }
        // Melenon.
        13 -> {
            val cam = camino {
                mueve(CX - 21f, CIMA + 4f)
                curva2(CX - 19f, CIMA - 11f, CX + 19f, CIMA - 11f, CX + 21f, CIMA + 4f)
                curva2(CX + 29f, CIMA + 26f, CX + 26f, CIMA + 40f, CX + 20f, CIMA + 42f)
                curva2(CX + 22f, CIMA + 24f, CX + 16f, CIMA + 12f, CX, CIMA + 10f)
                curva2(CX - 16f, CIMA + 12f, CX - 22f, CIMA + 24f, CX - 20f, CIMA + 42f)
                curva2(CX - 26f, CIMA + 40f, CX - 29f, CIMA + 26f, CX - 21f, CIMA + 4f)
                cierra()
            }
            pieza(cam, pelo, grosor = 2f)
        }
        // De punta, estilo susto.
        14 -> {
            (0..6).forEach { i ->
                val x = CX - 18f + i * 6f
                val alto = if (i % 2 == 0) 13f else 9f
                poligono(
                    listOf(
                        x - 3f to CIMA + 4f,
                        x to CIMA + 4f - alto,
                        x + 3f to CIMA + 4f
                    ),
                    pelo, grosor = 1.6f
                )
            }
        }
        // Dos trenzas.
        15 -> {
            val base = camino {
                mueve(CX - 20f, CIMA + 6f)
                curva2(CX - 17f, CIMA - 9f, CX + 17f, CIMA - 9f, CX + 20f, CIMA + 6f)
                curva2(CX + 8f, CIMA + 2f, CX - 8f, CIMA + 2f, CX - 20f, CIMA + 6f)
                cierra()
            }
            pieza(base, pelo, grosor = 2f)
            listOf(-21f, 21f).forEach { dx ->
                (0..2).forEach { i ->
                    circulo(CX + dx, CIMA + 8f + i * 6f, 4.2f, pelo)
                    circuloContorno(CX + dx, CIMA + 8f + i * 6f, 4.2f, grosor = 1.4f)
                }
                circulo(CX + dx, CIMA + 25f, 2.4f, Tinta.ROSA)
            }
        }
    }
}

fun Pincel.dibujaTocado(indice: Int) {
    when (indice) {
        0 -> Unit
        // Boina castiza.
        1 -> {
            val cam = camino {
                mueve(CX - 21f, CIMA + 4f)
                curva2(CX - 22f, CIMA - 8f, CX + 20f, CIMA - 10f, CX + 21f, CIMA + 3f)
                curva2(CX + 10f, CIMA + 7f, CX - 10f, CIMA + 7f, CX - 21f, CIMA + 4f)
                cierra()
            }
            pieza(cam, Tinta.NEGRA.copy(alpha = 0.92f), grosor = 2.2f)
            circulo(CX + 1f, CIMA - 9f, 2.4f, Tinta.NEGRA)
        }
        // Gorra de visera.
        2 -> {
            val copa = camino {
                mueve(CX - 19f, CIMA + 3f)
                curva2(CX - 19f, CIMA - 12f, CX + 19f, CIMA - 12f, CX + 19f, CIMA + 3f)
                cierra()
            }
            pieza(copa, Tinta.ROJA, grosor = 2.2f)
            val visera = camino {
                mueve(CX + 17f, CIMA + 2f)
                curva2(CX + 34f, CIMA + 1f, CX + 36f, CIMA + 8f, CX + 32f, CIMA + 9f)
                curva2(CX + 26f, CIMA + 6f, CX + 22f, CIMA + 6f, CX + 17f, CIMA + 6f)
                cierra()
            }
            pieza(visera, Tinta.ROJA, grosor = 2.2f)
            circulo(CX, CIMA - 11f, 2.2f, Tinta.BLANCA)
        }
        // Gorra al reves, muy de after.
        3 -> {
            val copa = camino {
                mueve(CX - 19f, CIMA + 3f)
                curva2(CX - 19f, CIMA - 12f, CX + 19f, CIMA - 12f, CX + 19f, CIMA + 3f)
                cierra()
            }
            pieza(copa, Tinta.AZUL, grosor = 2.2f)
            val visera = camino {
                mueve(CX - 17f, CIMA + 2f)
                curva2(CX - 34f, CIMA + 1f, CX - 36f, CIMA + 8f, CX - 32f, CIMA + 9f)
                curva2(CX - 26f, CIMA + 6f, CX - 22f, CIMA + 6f, CX - 17f, CIMA + 6f)
                cierra()
            }
            pieza(visera, Tinta.AZUL, grosor = 2.2f)
        }
        // Sombrero de paja de verano.
        4 -> {
            ovalo(CX, CIMA + 2f, 34f, 8f, Tinta.AMARILLA)
            ovaloContorno(CX, CIMA + 2f, 34f, 8f, grosor = 2.4f)
            val copa = camino {
                mueve(CX - 15f, CIMA + 1f)
                curva2(CX - 13f, CIMA - 14f, CX + 13f, CIMA - 14f, CX + 15f, CIMA + 1f)
                cierra()
            }
            pieza(copa, Tinta.AMARILLA, grosor = 2.4f)
            linea(CX - 15f, CIMA - 3f, CX + 15f, CIMA - 3f, color = Tinta.ROJA, grosor = 3.4f)
        }
        // Corona del que mas pone.
        5 -> {
            val cam = camino {
                mueve(CX - 17f, CIMA + 3f)
                recta(CX - 20f, CIMA - 14f)
                recta(CX - 9f, CIMA - 5f)
                recta(CX, CIMA - 17f)
                recta(CX + 9f, CIMA - 5f)
                recta(CX + 20f, CIMA - 14f)
                recta(CX + 17f, CIMA + 3f)
                cierra()
            }
            pieza(cam, Tinta.DORADA, grosor = 2.4f)
            circulo(CX, CIMA - 2f, 2.4f, Tinta.ROJA)
            circulo(CX - 11f, CIMA - 1f, 1.8f, Tinta.AZUL)
            circulo(CX + 11f, CIMA - 1f, 1.8f, Tinta.VERDE)
        }
        // Casco de obra: el que arregla las cuentas.
        6 -> {
            val cam = camino {
                mueve(CX - 22f, CIMA + 4f)
                curva2(CX - 20f, CIMA - 14f, CX + 20f, CIMA - 14f, CX + 22f, CIMA + 4f)
                cierra()
            }
            pieza(cam, Tinta.AMARILLA, grosor = 2.4f)
            ovalo(CX, CIMA + 4f, 26f, 3.6f, Tinta.AMARILLA)
            ovaloContorno(CX, CIMA + 4f, 26f, 3.6f, grosor = 2f)
            linea(CX, CIMA - 12f, CX, CIMA + 2f, color = Tinta.NEGRA.copy(alpha = 0.5f), grosor = 2f)
        }
        // Cono de trafico: nivel de fiesta alto.
        7 -> {
            val cono = camino {
                mueve(CX - 13f, CIMA + 3f)
                recta(CX - 3f, CIMA - 22f)
                recta(CX + 3f, CIMA - 22f)
                recta(CX + 13f, CIMA + 3f)
                cierra()
            }
            pieza(cono, Tinta.NARANJA, grosor = 2.4f)
            caja(CX, CIMA - 9f, 17f, 5f, 1f, Tinta.BLANCA)
            caja(CX, CIMA + 4f, 30f, 5f, 1.5f, Tinta.NARANJA)
            caja(CX, CIMA + 4f, 30f, 5f, 1.5f, Tinta.NEGRA, soloContorno = true, grosor = 2f)
        }
        // Gorro de cumpleanos.
        8 -> {
            val cono = camino {
                mueve(CX - 12f, CIMA + 3f)
                recta(CX + 2f, CIMA - 24f)
                recta(CX + 13f, CIMA + 1f)
                cierra()
            }
            pieza(cono, Tinta.ROSA, grosor = 2.2f)
            linea(CX - 7f, CIMA - 4f, CX + 8f, CIMA - 7f, color = Tinta.AMARILLA, grosor = 2.6f)
            linea(CX - 3f, CIMA - 13f, CX + 6f, CIMA - 15f, color = Tinta.AZUL, grosor = 2.2f)
            circulo(CX + 2f, CIMA - 26f, 3.4f, Tinta.AMARILLA)
            circuloContorno(CX + 2f, CIMA - 26f, 3.4f, grosor = 1.6f)
        }
        // Aureola del que paga sin que le pidan.
        9 -> {
            ovaloContorno(CX, CIMA - 13f, 15f, 4.5f, color = Tinta.DORADA, grosor = 3.2f)
            ovaloContorno(CX, CIMA - 13f, 15f, 4.5f, color = Tinta.AMARILLA.copy(alpha = 0.5f), grosor = 5.5f)
        }
        // Cuernos del que nunca paga.
        10 -> {
            listOf(-1f, 1f).forEach { lado ->
                val cam = camino {
                    mueve(CX + lado * 12f, CIMA + 2f)
                    curva2(
                        CX + lado * 20f, CIMA - 4f,
                        CX + lado * 19f, CIMA - 13f,
                        CX + lado * 14f, CIMA - 15f
                    )
                    curva2(
                        CX + lado * 15f, CIMA - 8f,
                        CX + lado * 12f, CIMA - 3f,
                        CX + lado * 8f, CIMA + 1f
                    )
                    cierra()
                }
                pieza(cam, Tinta.ROJA, grosor = 2f)
            }
        }
        // Sarten: el cocinero del grupo.
        11 -> {
            circulo(CX, CIMA - 3f, 15f, Tinta.GRIS)
            circuloContorno(CX, CIMA - 3f, 15f, grosor = 2.6f)
            circulo(CX, CIMA - 3f, 11f, Tinta.NEGRA.copy(alpha = 0.75f))
            caja(CX + 26f, CIMA - 3f, 24f, 5f, 2.5f, Tinta.NEGRA)
        }
        // Cubo en la cabeza.
        12 -> {
            val cam = camino {
                mueve(CX - 18f, CIMA + 4f)
                recta(CX - 14f, CIMA - 18f)
                recta(CX + 14f, CIMA - 18f)
                recta(CX + 18f, CIMA + 4f)
                cierra()
            }
            pieza(cam, Tinta.AZUL.copy(alpha = 0.9f), grosor = 2.4f)
            ovalo(CX, CIMA - 18f, 14f, 4f, Tinta.AZUL)
            ovaloContorno(CX, CIMA - 18f, 14f, 4f, grosor = 2.2f)
        }
        // Txapela.
        13 -> {
            ovalo(CX, CIMA, 24f, 7f, Tinta.NEGRA.copy(alpha = 0.9f))
            ovaloContorno(CX, CIMA, 24f, 7f, grosor = 2.2f)
            ovalo(CX, CIMA - 4f, 17f, 6f, Tinta.NEGRA.copy(alpha = 0.9f))
            ovaloContorno(CX, CIMA - 4f, 17f, 6f, grosor = 2.2f)
        }
        // Diadema con lazo.
        14 -> {
            arco(CX, CIMA + 6f, 21f, 14f, 195f, 150f, color = Tinta.ROSA, grosor = 3.4f)
            listOf(-1f, 1f).forEach { lado ->
                val lazo = camino {
                    mueve(CX + lado * 15f, CIMA - 6f)
                    curva2(
                        CX + lado * 24f, CIMA - 13f,
                        CX + lado * 25f, CIMA - 3f,
                        CX + lado * 16f, CIMA - 3f
                    )
                    cierra()
                }
                pieza(lazo, Tinta.ROSA, grosor = 1.8f)
            }
            circulo(CX, CIMA - 5f, 2.6f, Tinta.ROSA)
        }
        // Gorro de dormir del que se hace el dormido cuando llega la cuenta.
        15 -> {
            val cam = camino {
                mueve(CX - 19f, CIMA + 3f)
                curva2(CX - 17f, CIMA - 12f, CX + 4f, CIMA - 16f, CX + 14f, CIMA - 18f)
                curva2(CX + 22f, CIMA - 20f, CX + 24f, CIMA - 12f, CX + 19f, CIMA + 2f)
                curva2(CX + 8f, CIMA + 6f, CX - 8f, CIMA + 6f, CX - 19f, CIMA + 3f)
                cierra()
            }
            pieza(cam, Tinta.AZUL, grosor = 2.2f)
            circulo(CX + 20f, CIMA - 20f, 4.4f, Tinta.BLANCA)
            circuloContorno(CX + 20f, CIMA - 20f, 4.4f, grosor = 1.8f)
            caja(CX, CIMA + 3f, 40f, 6f, 3f, Tinta.BLANCA)
            caja(CX, CIMA + 3f, 40f, 6f, 3f, Tinta.NEGRA, soloContorno = true, grosor = 2f)
        }
    }
}
