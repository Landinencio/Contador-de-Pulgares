package com.pulgares.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pulgares.app.avatar.AvatarMonigote
import com.pulgares.app.data.red.Sincronizador
import com.pulgares.app.domain.model.Grupo
import com.pulgares.app.ui.components.BotonPegatina
import com.pulgares.app.ui.components.BotonRedondo
import com.pulgares.app.ui.components.Chapa
import com.pulgares.app.ui.components.DialogoConfirmar
import com.pulgares.app.ui.components.Pegatina
import com.pulgares.app.ui.theme.Paleta

/**
 * El bloque de "compartir el grupo" que va dentro de los ajustes. Si el grupo
 * solo vive en este móvil ofrece subirlo; si ya está compartido, enseña el código
 * y deja sincronizar, cambiarlo o dejar de compartir.
 */
@Composable
fun BloqueCompartir(
    grupo: Grupo,
    disponible: Boolean,
    sincronizando: Boolean,
    codigoRecuperacion: String?,
    onCompartir: () -> Unit,
    onSincronizar: () -> Unit,
    onRotarCodigo: () -> Unit,
    onDejarDeCompartir: () -> Unit,
    onCopiarCodigo: (String) -> Unit
) {
    var rotando by remember { mutableStateOf(false) }
    var dejando by remember { mutableStateOf(false) }

    Pegatina(
        modifier = Modifier.fillMaxWidth(),
        color = if (grupo.compartido) Paleta.VerdePazSuave else MaterialTheme.colorScheme.surface,
        sombra = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (grupo.compartido) "Grupo compartido" else "Compartir el grupo",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!disponible) {
                Text(
                    text = "Esta versión de la app no lleva sincronización, así que las " +
                        "cuentas se quedan en este móvil.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
                return@Column
            }

            if (!grupo.compartido) {
                Text(
                    text = "Sube el grupo y sale un código de seis letras. Quien lo teclee en " +
                        "su móvil verá los mismos gastos que tú.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Spacer(Modifier.height(12.dp))
                BotonPegatina(
                    texto = if (sincronizando) "Subiendo…" else "Compartir",
                    emoji = "🔗",
                    habilitado = !sincronizando,
                    onClick = onCompartir,
                    modifier = Modifier.fillMaxWidth()
                )
                return@Column
            }

            // ---- ya compartido ----
            Text(
                text = "Dale este código a quien quieras meter en el grupo:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(Modifier.height(10.dp))
            Pegatina(
                modifier = Modifier.fillMaxWidth(),
                color = Paleta.Papel,
                sombra = 3.dp,
                onClick = { grupo.codigo?.let(onCopiarCodigo) }
            ) {
                Text(
                    text = grupo.codigo.orEmpty(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = Paleta.Tinta,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
            }
            Text(
                text = "Toca el código para copiarlo.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(14.dp))
            BotonPegatina(
                texto = if (sincronizando) "Sincronizando…" else "Sincronizar ahora",
                emoji = "🔄",
                habilitado = !sincronizando,
                onClick = onSincronizar,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonPegatina(
                    texto = "Cambiar código",
                    color = Paleta.MostazaPulgar,
                    colorTexto = Paleta.Tinta,
                    habilitado = !sincronizando,
                    onClick = { rotando = true },
                    modifier = Modifier.weight(1f)
                )
                BotonPegatina(
                    texto = "Dejar de compartir",
                    color = Paleta.CremaHundido,
                    colorTexto = Paleta.Tinta,
                    habilitado = !sincronizando,
                    onClick = { dejando = true },
                    modifier = Modifier.weight(1f)
                )
            }

            if (codigoRecuperacion != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Código de recuperación",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Guárdalo fuera del móvil. Es lo único que te devuelve el mando " +
                        "del grupo si cambias de teléfono.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Chapa(texto = codigoRecuperacion, color = Paleta.MostazaSuave)
            }
        }
    }

    if (rotando) {
        DialogoConfirmar(
            titulo = "¿Cambiar el código?",
            mensaje = "El código de ahora deja de funcionar al instante. Sirve para cuando " +
                "alguien que ya no está en el grupo se lo apuntó. Los que ya están dentro " +
                "siguen dentro.",
            textoConfirmar = "Cambiarlo",
            emoji = "🔑",
            colorConfirmar = Paleta.MostazaPulgar,
            onConfirmar = {
                rotando = false
                onRotarCodigo()
            },
            onCancelar = { rotando = false }
        )
    }

    if (dejando) {
        DialogoConfirmar(
            titulo = "¿Dejar de compartir?",
            mensaje = "El grupo se queda solo en este móvil con todos sus gastos. Los demás " +
                "conservan su copia, pero ya no os veréis los cambios.",
            textoConfirmar = "Dejar de compartir",
            emoji = "✂️",
            onConfirmar = {
                dejando = false
                onDejarDeCompartir()
            },
            onCancelar = { dejando = false }
        )
    }
}

/**
 * Unirse a un grupo con un código. Dos pasos, como en el backend: primero se mira
 * qué hay detrás del código y luego se dice quién eres, para no acabar con dos
 * "Ana" en la lista.
 */
@Composable
fun DialogoUnirse(
    mirado: Sincronizador.GrupoRemoto?,
    sincronizando: Boolean,
    onMirar: (String) -> Unit,
    onEntrar: (codigo: String, colegaId: String?, nombre: String?) -> Unit,
    onCerrar: () -> Unit
) {
    var codigo by rememberSaveable { mutableStateOf("") }
    var nombreNuevo by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = onCerrar) {
        Pegatina(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            radio = 26.dp,
            sombra = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "🔗", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (mirado == null) "Unirse a un grupo" else mirado.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (mirado == null) {
                    Text(
                        text = "Teclea el código que te han pasado, o pega el mensaje entero.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it.uppercase() },
                        label = { Text("Código") },
                        placeholder = { Text("PAGA42") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BotonPegatina(
                            texto = "Cancelar",
                            color = Paleta.CremaHundido,
                            colorTexto = Paleta.Tinta,
                            onClick = onCerrar,
                            modifier = Modifier.weight(1f)
                        )
                        BotonPegatina(
                            texto = if (sincronizando) "Buscando…" else "Buscar",
                            habilitado = codigoValido(codigo) && !sincronizando,
                            onClick = { onMirar(limpiaCodigo(codigo)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // ---- ¿quién eres? ----
                    val libres = mirado.colegas.filter { it.id in mirado.colegasLibres }
                    Text(
                        text = if (libres.isEmpty()) {
                            "Nadie libre en la lista: entra como alguien nuevo."
                        } else {
                            "¿Cuál eres tú?"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(Modifier.height(10.dp))

                    libres.forEach { colega ->
                        Pegatina(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            sombra = 2.dp,
                            onClick = { onEntrar(limpiaCodigo(codigo), colega.id, null) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarMonigote(
                                    monigote = avatarDe(colega),
                                    tamano = 40,
                                    descripcion = colega.nombre
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = colega.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(text = "›", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nombreNuevo,
                        onValueChange = { nombreNuevo = it },
                        label = { Text("O soy alguien nuevo") },
                        placeholder = { Text("Tu nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BotonPegatina(
                            texto = "Cancelar",
                            color = Paleta.CremaHundido,
                            colorTexto = Paleta.Tinta,
                            onClick = onCerrar,
                            modifier = Modifier.weight(1f)
                        )
                        BotonPegatina(
                            texto = if (sincronizando) "Entrando…" else "Entrar",
                            habilitado = nombreNuevo.isNotBlank() && !sincronizando,
                            onClick = { onEntrar(limpiaCodigo(codigo), null, nombreNuevo.trim()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/** Igual que el backend: mayúsculas, sin espacios ni guiones. */
private fun limpiaCodigo(bruto: String): String =
    bruto.trim().uppercase().replace("-", "").replace(" ", "")

private fun codigoValido(bruto: String): Boolean {
    val limpio = limpiaCodigo(bruto)
    return limpio.length in 4..10 && limpio.all { it.isLetterOrDigit() }
}
