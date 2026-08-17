package com.pulgares.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta del Contador de Pulgares, sacada del monigote rosa que da nombre a
 * todo esto: rosa empolvado, papel crema y tinta negra de rotulador gordo.
 */
object Paleta {
    // El monigote y su familia de rosas.
    val RosaMonigote = Color(0xFFF3C2CD)
    val RosaMonigoteSombra = Color(0xFFE3A4B3)
    val RosaChicle = Color(0xFFFF5C8A)
    val RosaChicleOscuro = Color(0xFFD63E6B)

    // Papel y tinta.
    val Crema = Color(0xFFFBF5EA)
    val CremaHundido = Color(0xFFF2E9D9)
    val Tinta = Color(0xFF17161A)
    val TintaSuave = Color(0xFF56525C)
    val Papel = Color(0xFFFFFDF8)

    // Semaforo de las deudas.
    val VerdePaz = Color(0xFF35B87A)
    val VerdePazSuave = Color(0xFFD3F3E2)
    val RojoDeuda = Color(0xFFF2504B)
    val RojoDeudaSuave = Color(0xFFFFDCDA)
    val MostazaPulgar = Color(0xFFFFC53D)
    val MostazaSuave = Color(0xFFFFF0C7)

    // Colorines de apoyo (categorias, fondos de avatar, graficas).
    val AzulPitufo = Color(0xFF5BB8FF)
    val MoradoUva = Color(0xFFA98BFF)
    val NaranjaGamba = Color(0xFFFF8A4C)
    val TurquesaPiscina = Color(0xFF3FD1C7)
    val VerdeMoco = Color(0xFF9BD44B)
    val MarronCroqueta = Color(0xFFC08552)

    // Modo oscuro: noche de after, pero que se lea.
    val NocheFondo = Color(0xFF1B1720)
    val NocheTarjeta = Color(0xFF272130)
    val NocheTinta = Color(0xFFF6EFE6)
    val NocheTintaSuave = Color(0xFFB3A9BD)

    /** Colorines de las categorias de gasto, en el orden del enum. */
    val categorias = listOf(
        MostazaPulgar,   // birras
        NaranjaGamba,    // comida
        AzulPitufo,      // casa
        TurquesaPiscina, // viaje
        MostazaPulgar,   // taxi
        RosaChicle,      // fiesta
        VerdeMoco,       // compra
        MoradoUva,       // regalo
        RojoDeuda,       // resaca
        TintaSuave       // misterio
    )
}
