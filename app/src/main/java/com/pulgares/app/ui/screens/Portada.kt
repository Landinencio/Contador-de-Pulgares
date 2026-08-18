package com.pulgares.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.avatar.MascotaPulgares
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.data.ResumenGrupo
import com.pulgares.app.domain.model.Dinero
import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento
import com.pulgares.app.ui.components.BotonPegatina
import com.pulgares.app.ui.components.BotonRedondo
import com.pulgares.app.ui.components.Chapa
import com.pulgares.app.ui.components.Pegatina
import com.pulgares.app.ui.theme.Paleta

/**
 * La portada: mi monigote, el resumen de si debo o me deben, y los grupos.
 * Lo primero que se ve al abrir es cuanto dinero hay en juego y una coña.
 */
@Composable
fun PortadaScreen(
    grupos: List<ResumenGrupo>,
    miAvatar: Monigote,
    onAbrirGrupo: (String) -> Unit,
    onNuevoGrupo: () -> Unit,
    onEditarAvatar: () -> Unit,
    /** null si esta build no lleva sincronización: entonces no se ofrece. */
    onUnirse: (() -> Unit)? = null,
    /** Estado del Cobrador del Frac; null mientras se carga. */
    cobradorContratado: Boolean? = null,
    onContratarCobrador: () -> Unit = {},
    onDespedirCobrador: () -> Unit = {}
) {
    val deboTotal = grupos.filter { it.miNeto < 0 }.sumOf { -it.miNeto }
    val meDebenTotal = grupos.filter { it.miNeto > 0 }.sumOf { it.miNeto }
    // "Todo en paz" es del grupo entero, no solo de mi saldo: si Luis le debe a
    // Ana y yo estoy a cero, aquí no hay ninguna paz que celebrar.
    val todoSaldado = grupos.isNotEmpty() && grupos.all { it.enPaz }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---- cabecera con mi monigote ----
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pegatina(
                    radio = 50.dp,
                    sombra = 4.dp,
                    color = Paleta.RosaMonigote,
                    onClick = onEditarAvatar
                ) {
                    AvatarMonigote(
                        monigote = miAvatar,
                        tamano = 62,
                        conFondo = false,
                        descripcion = "Tu monigote. Toca para cambiarlo."
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contador de Pulgares",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Sin anuncios, sin límites, sin piedad",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---- el resumen gordo ----
        item {
            ResumenPersonal(
                deboTotal = deboTotal,
                meDebenTotal = meDebenTotal,
                todoSaldado = todoSaldado
            )
        }

        // ---- grupos ----
        if (grupos.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MascotaPulgares(tamano = 150)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = Frases.para(Momento.SIN_GRUPOS, semilla = 1),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "Tus grupos",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(grupos, key = { it.grupo.id }) { resumen ->
                FilaGrupo(resumen = resumen, onClick = { onAbrirGrupo(resumen.grupo.id) })
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            BotonPegatina(
                texto = "Nuevo grupo",
                emoji = "👥",
                onClick = onNuevoGrupo,
                modifier = Modifier.fillMaxWidth()
            )
            if (onUnirse != null) {
                Spacer(Modifier.height(10.dp))
                BotonPegatina(
                    texto = "Unirme con un código",
                    emoji = "🔗",
                    color = Paleta.MostazaPulgar,
                    colorTexto = Paleta.Tinta,
                    onClick = onUnirse,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ---- el Cobrador del Frac ----
        if (cobradorContratado != null) {
            item {
                TarjetaCobrador(
                    contratado = cobradorContratado,
                    onContratar = onContratarCobrador,
                    onDespedir = onDespedirCobrador
                )
            }
        }
    }
}

/** El cartel de "debes X" / "te deben Y", con su coña correspondiente. */
@Composable
private fun ResumenPersonal(deboTotal: Long, meDebenTotal: Long, todoSaldado: Boolean) {
    val yoEstoyACero = deboTotal == 0L && meDebenTotal == 0L
    val enPaz = yoEstoyACero && todoSaldado
    val color = when {
        enPaz -> Paleta.VerdePazSuave
        deboTotal > meDebenTotal -> Paleta.RojoDeudaSuave
        else -> Paleta.MostazaSuave
    }
    val frase = when {
        enPaz -> Frases.para(Momento.EN_PAZ, semilla = 2)
        yoEstoyACero -> "Tú estás a cero. Los demás, allá ellos."
        deboTotal > meDebenTotal -> Frases.para(
            Momento.CABECERA_DEBO,
            centimos = deboTotal,
            semilla = deboTotal
        )
        else -> Frases.para(
            Momento.CABECERA_ME_DEBEN,
            centimos = meDebenTotal,
            semilla = meDebenTotal
        )
    }

    Pegatina(color = color, sombra = 5.dp, radio = 26.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = frase,
                style = MaterialTheme.typography.titleLarge,
                color = Paleta.Tinta
            )
            if (!yoEstoyACero) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (deboTotal > 0) {
                        Chapa(texto = "Debes ${Dinero.formatea(deboTotal)}", color = Paleta.RojoDeuda, colorTexto = Paleta.Papel)
                    }
                    if (meDebenTotal > 0) {
                        Chapa(texto = "Te deben ${Dinero.formatea(meDebenTotal)}", color = Paleta.VerdePaz, colorTexto = Paleta.Papel)
                    }
                }
                Spacer(Modifier.height(8.dp))
                // El guiño de las pesetas: el grupo sigue pensando en ellas.
                Text(
                    text = pesetasDeLasDeAntes(deboTotal, meDebenTotal),
                    style = MaterialTheme.typography.labelMedium,
                    color = Paleta.TintaSuave
                )
            }
        }
    }
}

/** Una fila de grupo: emoji, nombre, cuántos gastos y mi saldo ahí. */
@Composable
private fun FilaGrupo(resumen: ResumenGrupo, onClick: () -> Unit) {
    Pegatina(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        sombra = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(46.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = resumen.grupo.emoji, style = MaterialTheme.typography.displayMedium)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resumen.grupo.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = detalleGrupo(resumen),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            when {
                resumen.enPaz -> Chapa(texto = "En paz", color = Paleta.VerdePazSuave)
                resumen.miNeto < 0 -> Chapa(
                    texto = "-${Dinero.formatea(-resumen.miNeto)}",
                    color = Paleta.RojoDeuda,
                    colorTexto = Paleta.Papel
                )
                resumen.miNeto > 0 -> Chapa(
                    texto = "+${Dinero.formatea(resumen.miNeto)}",
                    color = Paleta.VerdePaz,
                    colorTexto = Paleta.Papel
                )
                else -> Chapa(texto = "A cero", color = Paleta.CremaHundido)
            }
        }
    }
}

/** "O sea, 3.893 pesetas de las de antes." Para el grupo de siempre. */
private fun pesetasDeLasDeAntes(debo: Long, meDeben: Long): String = when {
    debo > 0 && meDeben > 0 ->
        "O sea: debes ${Dinero.formateaPesetas(debo)} y te deben ${Dinero.formateaPesetas(meDeben)}, de las de antes"
    debo > 0 -> "O sea, ${Dinero.formateaPesetas(debo)} de las de antes"
    else -> "O sea, ${Dinero.formateaPesetas(meDeben)} de las de antes"
}

private fun detalleGrupo(resumen: ResumenGrupo): String {
    val gente = resumen.grupo.colegas.size
    val gastos = when (resumen.cuantosGastos) {
        0 -> "sin gastos"
        1 -> "1 gasto"
        else -> "${resumen.cuantosGastos} gastos"
    }
    val total = if (resumen.totalGastado > 0) {
        " · ${Dinero.formateaCorto(resumen.totalGastado)} (${Dinero.formateaPesetas(resumen.totalGastado)})"
    } else {
        ""
    }
    return "$gente colegas · $gastos$total"
}

/**
 * La tarjeta del Cobrador del Frac: un caballero con chistera que recuerda las
 * deudas por notificación. Elegante y no pesado: como mucho un aviso cada dos
 * días, y solo si de verdad debes algo.
 */
@Composable
private fun TarjetaCobrador(
    contratado: Boolean,
    onContratar: () -> Unit,
    onDespedir: () -> Unit
) {
    Pegatina(
        modifier = Modifier.fillMaxWidth(),
        color = if (contratado) Paleta.CremaHundido else Paleta.MostazaSuave,
        sombra = 4.dp,
        radio = 24.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarMonigote(
                monigote = Monigote.ELCOBRADOR,
                tamano = 74,
                conFondo = false,
                recortadoRedondo = false,
                baila = !contratado,
                descripcion = "El Cobrador del Frac"
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // El fondo de esta tarjeta es SIEMPRE claro (mostaza o crema),
                // asi que la tinta va fija: con el color del tema, en modo
                // oscuro el titulo salia blanco sobre amarillo (invisible).
                Text(
                    text = "El Cobrador del Frac",
                    style = MaterialTheme.typography.titleLarge,
                    color = Paleta.Tinta
                )
                Text(
                    text = if (contratado) {
                        "En nómina. Si debes algo, te lo recordará con retranca: " +
                            "como mucho un aviso cada dos días."
                    } else {
                        "Contrátalo y te recordará tus deudas por notificación, " +
                            "con la elegancia de un caballero y la paciencia de un acreedor."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Paleta.TintaSuave
                )
                Spacer(Modifier.height(10.dp))
                BotonPegatina(
                    texto = if (contratado) "Despedirlo" else "Contratarlo",
                    emoji = "🎩",
                    color = if (contratado) Paleta.CremaHundido else Paleta.RosaChicle,
                    colorTexto = if (contratado) Paleta.Tinta else Paleta.Papel,
                    onClick = if (contratado) onDespedir else onContratar,
                    sombra = 3.dp
                )
            }
        }
    }
}
