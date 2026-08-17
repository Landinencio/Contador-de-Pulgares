package com.pulgares.app.avatar

import kotlin.random.Random

/**
 * Configuracion de un monigote. Cada dimension es un indice sobre su catalogo;
 * los catalogos viven en [Catalogos] y los dibuja [dibujaMonigote].
 *
 * Se serializa como "m1:0,3,5,..." para guardarlo en Room o mandarlo por la red
 * sin JSON. Si una version futura anade dimensiones, sube el prefijo y las
 * configuraciones viejas se leen con valores por defecto en lo que falte.
 */
data class Monigote(
    val forma: Int = 0,
    val color: Int = 1,
    val ojos: Int = 0,
    val boca: Int = 0,
    val pelo: Int = 0,
    val tocado: Int = 0,
    val gafas: Int = 0,
    val barba: Int = 0,
    val accesorio: Int = 0,
    val marca: Int = 0,
    val fondo: Int = 0
) {

    fun serializa(): String = "m1:" + listOf(
        forma, color, ojos, boca, pelo, tocado, gafas, barba, accesorio, marca, fondo
    ).joinToString(",")

    /** Rota una dimension concreta (para las flechas del editor). */
    fun cambia(dimension: Dimension, delta: Int): Monigote {
        val actual = valorDe(dimension)
        val total = dimension.cuantos
        val nuevo = ((actual + delta) % total + total) % total
        return conValor(dimension, nuevo)
    }

    fun valorDe(dimension: Dimension): Int = when (dimension) {
        Dimension.FORMA -> forma
        Dimension.COLOR -> color
        Dimension.OJOS -> ojos
        Dimension.BOCA -> boca
        Dimension.PELO -> pelo
        Dimension.TOCADO -> tocado
        Dimension.GAFAS -> gafas
        Dimension.BARBA -> barba
        Dimension.ACCESORIO -> accesorio
        Dimension.MARCA -> marca
        Dimension.FONDO -> fondo
    }

    fun conValor(dimension: Dimension, valor: Int): Monigote {
        val v = valor.coerceIn(0, dimension.cuantos - 1)
        return when (dimension) {
            Dimension.FORMA -> copy(forma = v)
            Dimension.COLOR -> copy(color = v)
            Dimension.OJOS -> copy(ojos = v)
            Dimension.BOCA -> copy(boca = v)
            Dimension.PELO -> copy(pelo = v)
            Dimension.TOCADO -> copy(tocado = v)
            Dimension.GAFAS -> copy(gafas = v)
            Dimension.BARBA -> copy(barba = v)
            Dimension.ACCESORIO -> copy(accesorio = v)
            Dimension.MARCA -> copy(marca = v)
            Dimension.FONDO -> copy(fondo = v)
        }
    }

    /** Nombre de la variante elegida en esa dimension ("Cresta", "Boina"...). */
    fun nombreDe(dimension: Dimension): String =
        dimension.nombres.getOrElse(valorDe(dimension)) { "?" }

    companion object {
        /**
         * El Cobrador del Frac: la mascota de las notificaciones de deuda. El
         * frac es el propio cuerpo (negro carbón) con la pechera de gala; el
         * resto son piezas normales del catálogo, así que cualquiera puede
         * vestir a su monigote de cobrador si le hace gracia.
         */
        val ELCOBRADOR = Monigote(
            forma = 0,
            color = 12,
            ojos = 1,
            boca = 13,
            pelo = 0,
            tocado = Catalogos.tocados.indexOf("Chistera"),
            gafas = Catalogos.gafas.indexOf("Monóculo"),
            barba = Catalogos.barbas.indexOf("Bigotón"),
            accesorio = Catalogos.accesorios.indexOf("Maletín"),
            marca = Catalogos.marcas.indexOf("Pechera de gala"),
            fondo = 0
        )

        /** El monigote de la casa: rosa, alubia y con cara de circunstancias. */
        val ELMONIGOTE = Monigote(
            forma = 0,
            color = 1,
            ojos = 0,
            boca = 10,
            pelo = 0,
            tocado = 0,
            gafas = 0,
            barba = 0,
            accesorio = 0,
            marca = 0,
            fondo = 0
        )

        fun parse(bruto: String?): Monigote? {
            if (bruto.isNullOrBlank() || !bruto.startsWith("m1:")) return null
            val partes = bruto.removePrefix("m1:").split(",").mapNotNull { it.trim().toIntOrNull() }
            if (partes.size < 11) return null
            return Monigote(
                forma = partes[0].coerceIn(0, Dimension.FORMA.cuantos - 1),
                color = partes[1].coerceIn(0, Dimension.COLOR.cuantos - 1),
                ojos = partes[2].coerceIn(0, Dimension.OJOS.cuantos - 1),
                boca = partes[3].coerceIn(0, Dimension.BOCA.cuantos - 1),
                pelo = partes[4].coerceIn(0, Dimension.PELO.cuantos - 1),
                tocado = partes[5].coerceIn(0, Dimension.TOCADO.cuantos - 1),
                gafas = partes[6].coerceIn(0, Dimension.GAFAS.cuantos - 1),
                barba = partes[7].coerceIn(0, Dimension.BARBA.cuantos - 1),
                accesorio = partes[8].coerceIn(0, Dimension.ACCESORIO.cuantos - 1),
                marca = partes[9].coerceIn(0, Dimension.MARCA.cuantos - 1),
                fondo = partes[10].coerceIn(0, Dimension.FONDO.cuantos - 1)
            )
        }

        /** Uno al azar de los muchos millones que hay. */
        fun aleatorio(random: Random = Random.Default): Monigote = Monigote(
            forma = random.nextInt(Dimension.FORMA.cuantos),
            color = random.nextInt(Dimension.COLOR.cuantos),
            ojos = random.nextInt(Dimension.OJOS.cuantos),
            boca = random.nextInt(Dimension.BOCA.cuantos),
            // Los "nada" (indice 0) salen mas a menudo para no cargar de cosas.
            pelo = quizas(random, Dimension.PELO.cuantos, 3),
            tocado = quizas(random, Dimension.TOCADO.cuantos, 3),
            gafas = quizas(random, Dimension.GAFAS.cuantos, 3),
            barba = quizas(random, Dimension.BARBA.cuantos, 4),
            accesorio = quizas(random, Dimension.ACCESORIO.cuantos, 2),
            marca = quizas(random, Dimension.MARCA.cuantos, 3),
            fondo = random.nextInt(Dimension.FONDO.cuantos)
        )

        /** 1 de cada [entre] veces devuelve 0 ("nada"); el resto, algo al azar. */
        private fun quizas(random: Random, cuantos: Int, entre: Int): Int =
            if (random.nextInt(entre) == 0) 0 else random.nextInt(cuantos)

        /** Un monigote estable a partir de un texto (para colegas sin avatar). */
        fun desdeSemilla(semilla: String): Monigote =
            aleatorio(Random(semilla.hashCode().toLong()))

        /** Cuantas pintas distintas se pueden montar. Sale en los ajustes. */
        val combinaciones: Long
            get() = Dimension.entries.fold(1L) { acumulado, d -> acumulado * d.cuantos }
    }
}

/** Las piezas que se pueden tocar en el editor, en el orden de las pestanas. */
enum class Dimension(val etiqueta: String, val nombres: List<String>) {
    FORMA("Cuerpo", Catalogos.formas),
    COLOR("Color", Catalogos.colores),
    OJOS("Ojos", Catalogos.ojos),
    BOCA("Boca", Catalogos.bocas),
    PELO("Pelo", Catalogos.pelos),
    TOCADO("Sombrero", Catalogos.tocados),
    GAFAS("Gafas", Catalogos.gafas),
    BARBA("Pelambrera", Catalogos.barbas),
    ACCESORIO("Cachivache", Catalogos.accesorios),
    MARCA("Detalles", Catalogos.marcas),
    FONDO("Fondo", Catalogos.fondos);

    val cuantos: Int get() = nombres.size
}

/**
 * Los nombres de todas las variantes. El orden manda: el indice de esta lista
 * es lo que se guarda, asi que se anaden variantes AL FINAL para no cambiarle
 * la cara a los avatares ya guardados.
 */
object Catalogos {

    val formas = listOf(
        "Alubia", "Patata", "Huevo", "Pera", "Croqueta", "Churro", "Bola", "Flan"
    )

    val colores = listOf(
        "Rosa chicle", "Rosa monigote", "Verde moco", "Azul pitufo", "Amarillo pollo",
        "Morado uva", "Naranja gamba", "Marrón croqueta", "Gris zombi", "Blanco fantasma",
        "Rojo tomate", "Turquesa piscina", "Negro carbón", "Dorado nuevo rico"
    )

    val ojos = listOf(
        "Normales", "Saltones", "Bizcos", "Dormido", "Enamorado", "Signos de euro",
        "Espiral", "Guiño", "Enfadado", "Con ojeras", "Rayos láser", "Estrellitas",
        "Fulminado", "Robot", "Llorón", "Entrecerrados"
    )

    val bocas = listOf(
        "Sonrisa", "Sonrisota", "Dientes", "Mueca", "Lengua fuera", "Silbando",
        "Colmillos", "Grito", "Beso", "Cremallera", "Sin boca", "Chupete",
        "Babeando", "Sonrisa torcida", "Ohhh", "Rechinando"
    )

    val pelos = listOf(
        "Nada", "Tres pelos", "Tupé", "Afro", "Cresta", "Moño", "Coleta", "Rizos",
        "Flequillo", "Mullet", "Rastas", "Calva brillante", "Con entradas",
        "Melenón", "De punta", "Trenzas"
    )

    val tocados = listOf(
        "Nada", "Boina", "Gorra", "Gorra al revés", "Sombrero de paja", "Corona",
        "Casco de obra", "Cono de tráfico", "Gorro de fiesta", "Aureola", "Cuernos",
        "Sartén", "Cubo", "Txapela", "Diadema", "Gorro de dormir", "Chistera"
    )

    val gafas = listOf(
        "Nada", "De sol", "De pasta", "Gafotas", "De esquí", "Antifaz",
        "Monóculo", "De cine 3D", "Nariz de Groucho", "De corazones",
        "Deportivas", "De soldador"
    )

    val barbas = listOf(
        "Nada", "Perilla", "Bigote", "Bigotón", "Barba cerrada", "Chuletas",
        "De náufrago", "Mosca", "Candado", "De vikingo"
    )

    val accesorios = listOf(
        "Nada", "Jarra de birra", "Churro", "Móvil", "Billete", "Tarjeta",
        "Calculadora", "Bocadillo", "Dado", "Pulgar gigante", "Cartera vacía",
        "Litrona", "Paraguas", "Porción de pizza", "Mando", "Pancarta", "Maletín"
    )

    val marcas = listOf(
        "Nada", "Sonrojo", "Pecas", "Ojeras", "Tirita", "Gota de sudor",
        "Lagrimón", "Chichón", "Granos", "Tatuaje de corazón", "Cicatriz", "Purpurina",
        "Pechera de gala"
    )

    val fondos = listOf(
        "Rosa liso", "Crema", "Azul", "Verde", "Lunares", "Rayas", "Estrellas",
        "Monedas", "Billetes", "Garabatos", "Rayos", "Atardecer", "Cuadrícula", "Confeti"
    )
}
