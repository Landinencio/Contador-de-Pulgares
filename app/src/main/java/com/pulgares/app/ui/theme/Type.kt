package com.pulgares.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pulgares.app.R

/**
 * Tipografias redonditas (licencia OFL, ver res/raw/ofl_*.txt): Fredoka para
 * los titulos gordos y Baloo 2 para el resto. Las dos son variables, asi que
 * cada peso se pide por FontVariation en vez de meter un TTF por peso.
 */

@OptIn(ExperimentalTextApi::class)
private fun variable(recurso: Int, peso: FontWeight) = Font(
    resId = recurso,
    weight = peso,
    variationSettings = FontVariation.Settings(FontVariation.weight(peso.weight))
)

val FuenteCuerpo = FontFamily(
    variable(R.font.baloo2, FontWeight.Normal),
    variable(R.font.baloo2, FontWeight.Medium),
    variable(R.font.baloo2, FontWeight.SemiBold),
    variable(R.font.baloo2, FontWeight.Bold),
    variable(R.font.baloo2, FontWeight.ExtraBold)
)

val FuenteTitulo = FontFamily(
    variable(R.font.fredoka, FontWeight.Normal),
    variable(R.font.fredoka, FontWeight.SemiBold),
    variable(R.font.fredoka, FontWeight.Bold)
)

val Tipografia = Typography(
    displayLarge = TextStyle(
        fontFamily = FuenteTitulo,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FuenteTitulo,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FuenteTitulo,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FuenteTitulo,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FuenteCuerpo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FuenteCuerpo,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FuenteCuerpo,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FuenteCuerpo,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FuenteCuerpo,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FuenteCuerpo,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 17.sp
    )
)
