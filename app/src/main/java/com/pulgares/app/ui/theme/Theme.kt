package com.pulgares.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** Esquinas generosas: aqui nada es cuadrado, ni las cuentas. */
val FormasRedonditas = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

private val ColoresDeDia = lightColorScheme(
    primary = Paleta.RosaChicle,
    onPrimary = Paleta.Papel,
    primaryContainer = Paleta.RosaMonigote,
    onPrimaryContainer = Paleta.Tinta,
    secondary = Paleta.MostazaPulgar,
    onSecondary = Paleta.Tinta,
    secondaryContainer = Paleta.MostazaSuave,
    onSecondaryContainer = Paleta.Tinta,
    tertiary = Paleta.AzulPitufo,
    onTertiary = Paleta.Tinta,
    background = Paleta.Crema,
    onBackground = Paleta.Tinta,
    surface = Paleta.Papel,
    onSurface = Paleta.Tinta,
    surfaceVariant = Paleta.CremaHundido,
    onSurfaceVariant = Paleta.TintaSuave,
    outline = Paleta.Tinta,
    error = Paleta.RojoDeuda,
    onError = Paleta.Papel,
    errorContainer = Paleta.RojoDeudaSuave,
    onErrorContainer = Paleta.Tinta
)

private val ColoresDeNoche = darkColorScheme(
    primary = Paleta.RosaChicle,
    onPrimary = Paleta.Tinta,
    primaryContainer = Paleta.RosaChicleOscuro,
    onPrimaryContainer = Paleta.NocheTinta,
    secondary = Paleta.MostazaPulgar,
    onSecondary = Paleta.Tinta,
    secondaryContainer = Paleta.MarronCroqueta,
    onSecondaryContainer = Paleta.NocheTinta,
    tertiary = Paleta.AzulPitufo,
    onTertiary = Paleta.Tinta,
    background = Paleta.NocheFondo,
    onBackground = Paleta.NocheTinta,
    surface = Paleta.NocheTarjeta,
    onSurface = Paleta.NocheTinta,
    surfaceVariant = Paleta.NocheTarjeta,
    onSurfaceVariant = Paleta.NocheTintaSuave,
    outline = Paleta.Tinta,
    error = Paleta.RojoDeuda,
    onError = Paleta.Papel,
    errorContainer = Paleta.RojoDeudaSuave,
    onErrorContainer = Paleta.Tinta
)

@Composable
fun TemaPulgares(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (oscuro) ColoresDeNoche else ColoresDeDia,
        typography = Tipografia,
        shapes = FormasRedonditas,
        content = content
    )
}
