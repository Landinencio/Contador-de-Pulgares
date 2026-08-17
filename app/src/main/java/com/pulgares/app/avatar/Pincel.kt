package com.pulgares.app.avatar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Pincel de dibujo en coordenadas de monigote: un cuadrado virtual de 100x100
 * donde el bicho vive siempre en el mismo sitio, sea el logo de 200dp o el
 * avatar de 24dp de una lista. Asi las piezas (ojos, gorros, churros) se
 * colocan con numeros fijos y todo encaja a cualquier tamano.
 */
class Pincel(
    private val scope: DrawScope,
    private val lado: Float,
    private val dx: Float,
    private val dy: Float,
    val tinta: Color,
    /** Grosor del rotulador, en unidades de monigote. */
    val grosorBase: Float = 3.2f
) {

    /** Un punto del mundo monigote a pixeles. */
    fun p(x: Float, y: Float) = Offset(dx + x / 100f * lado, dy + y / 100f * lado)

    /** Una medida (radio, grosor) del mundo monigote a pixeles. */
    fun u(valor: Float) = valor / 100f * lado

    private fun trazo(grosor: Float = grosorBase) = Stroke(
        width = u(grosor),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    // ---- primitivas ----

    fun linea(x1: Float, y1: Float, x2: Float, y2: Float, color: Color = tinta, grosor: Float = grosorBase) {
        scope.drawLine(color, p(x1, y1), p(x2, y2), strokeWidth = u(grosor), cap = StrokeCap.Round)
    }

    fun circulo(x: Float, y: Float, radio: Float, color: Color) {
        scope.drawCircle(color, radius = u(radio), center = p(x, y))
    }

    fun circuloContorno(x: Float, y: Float, radio: Float, color: Color = tinta, grosor: Float = grosorBase) {
        scope.drawCircle(color, radius = u(radio), center = p(x, y), style = trazo(grosor))
    }

    /** Ovalo centrado en (x,y) con semiejes [rx] y [ry]. */
    fun ovalo(x: Float, y: Float, rx: Float, ry: Float, color: Color) {
        scope.drawOval(color, topLeft = p(x - rx, y - ry), size = Size(u(rx * 2), u(ry * 2)))
    }

    fun ovaloContorno(x: Float, y: Float, rx: Float, ry: Float, color: Color = tinta, grosor: Float = grosorBase) {
        scope.drawOval(
            color,
            topLeft = p(x - rx, y - ry),
            size = Size(u(rx * 2), u(ry * 2)),
            style = trazo(grosor)
        )
    }

    /** Rectangulo redondeado centrado en (x,y). */
    fun caja(
        x: Float,
        y: Float,
        ancho: Float,
        alto: Float,
        radio: Float,
        color: Color,
        soloContorno: Boolean = false,
        grosor: Float = grosorBase
    ) {
        val topLeft = p(x - ancho / 2, y - alto / 2)
        val size = Size(u(ancho), u(alto))
        val esquina = CornerRadius(u(radio), u(radio))
        if (soloContorno) {
            scope.drawRoundRect(color, topLeft, size, esquina, style = trazo(grosor))
        } else {
            scope.drawRoundRect(color, topLeft, size, esquina)
        }
    }

    /** Arco: [desde] y [barrido] en grados, 0 = derecha, positivo = abajo. */
    fun arco(
        x: Float,
        y: Float,
        rx: Float,
        ry: Float,
        desde: Float,
        barrido: Float,
        color: Color = tinta,
        grosor: Float = grosorBase
    ) {
        scope.drawArc(
            color = color,
            startAngle = desde,
            sweepAngle = barrido,
            useCenter = false,
            topLeft = p(x - rx, y - ry),
            size = Size(u(rx * 2), u(ry * 2)),
            style = trazo(grosor)
        )
    }

    /** Sector relleno (para tartas, bocas abiertas, conos). */
    fun sector(x: Float, y: Float, rx: Float, ry: Float, desde: Float, barrido: Float, color: Color) {
        scope.drawArc(
            color = color,
            startAngle = desde,
            sweepAngle = barrido,
            useCenter = true,
            topLeft = p(x - rx, y - ry),
            size = Size(u(rx * 2), u(ry * 2))
        )
    }

    fun rellena(camino: Camino, color: Color) {
        scope.drawPath(camino.path, color)
    }

    fun contornea(camino: Camino, color: Color = tinta, grosor: Float = grosorBase) {
        scope.drawPath(camino.path, color, style = trazo(grosor))
    }

    /** Dibuja el camino con relleno y contorno de una pasada (lo mas comun). */
    fun pieza(camino: Camino, relleno: Color, borde: Color = tinta, grosor: Float = grosorBase) {
        rellena(camino, relleno)
        contornea(camino, borde, grosor)
    }

    /** Construye un camino en coordenadas de monigote. */
    fun camino(bloque: Camino.() -> Unit): Camino = Camino(this).apply(bloque)

    /** Un poligono/curva cerrada rapida a partir de puntos. */
    fun poligono(puntos: List<Pair<Float, Float>>, relleno: Color, borde: Color = tinta, grosor: Float = grosorBase) {
        if (puntos.size < 2) return
        val camino = camino {
            mueve(puntos.first().first, puntos.first().second)
            puntos.drop(1).forEach { recta(it.first, it.second) }
            cierra()
        }
        pieza(camino, relleno, borde, grosor)
    }

    /** Ejecuta [bloque] girado [grados] alrededor de (x,y) del mundo monigote. */
    fun girado(grados: Float, x: Float, y: Float, bloque: () -> Unit) {
        scope.rotate(grados, pivot = p(x, y)) { bloque() }
    }
}

/** Camino en coordenadas de monigote (0..100), sin pelearse con pixeles. */
class Camino(private val pincel: Pincel) {
    val path = Path()

    fun mueve(x: Float, y: Float) = apply { pincel.p(x, y).let { path.moveTo(it.x, it.y) } }

    fun recta(x: Float, y: Float) = apply { pincel.p(x, y).let { path.lineTo(it.x, it.y) } }

    /** Curva cuadratica: un punto de control y el destino. */
    fun curva(cx: Float, cy: Float, x: Float, y: Float) = apply {
        val c = pincel.p(cx, cy)
        val f = pincel.p(x, y)
        path.quadraticBezierTo(c.x, c.y, f.x, f.y)
    }

    /** Curva cubica: dos puntos de control y el destino. */
    fun curva2(c1x: Float, c1y: Float, c2x: Float, c2y: Float, x: Float, y: Float) = apply {
        val a = pincel.p(c1x, c1y)
        val b = pincel.p(c2x, c2y)
        val f = pincel.p(x, y)
        path.cubicTo(a.x, a.y, b.x, b.y, f.x, f.y)
    }

    fun cierra() = apply { path.close() }
}
