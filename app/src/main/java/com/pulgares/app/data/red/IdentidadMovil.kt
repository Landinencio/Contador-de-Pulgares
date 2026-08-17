package com.pulgares.app.data.red

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.ajustes by preferencesDataStore(name = "pulgares")

/**
 * El identificador de este móvil ante el backend. Un UUID que se genera la
 * primera vez y no se mueve de aquí: **no hay cuentas ni correos**. Sirve para
 * dos cosas y ninguna más — saber en qué grupos está este móvil y saber a qué
 * colega corresponde dentro de cada uno.
 *
 * Perderlo significa perder el acceso a los grupos desde este móvil, y para eso
 * está el código de recuperación que da el backend al crear un grupo.
 */
class IdentidadMovil(private val context: Context) {

    private val clave = stringPreferencesKey("uid")
    private val claveRecuperacion = stringPreferencesKey("recuperacion")

    /** El uid de este móvil; se crea al primer uso. */
    suspend fun uid(): String {
        val guardado = context.ajustes.data.first()[clave]
        if (!guardado.isNullOrBlank()) return guardado

        val nuevo = UUID.randomUUID().toString()
        context.ajustes.edit { it[clave] = nuevo }
        return nuevo
    }

    /**
     * Los códigos de recuperación de los grupos que ha creado este móvil, para
     * poder enseñárselos otra vez. Se guardan como "grupoId=codigo;..." porque
     * son dos o tres y no merece una tabla.
     */
    suspend fun guardaRecuperacion(remotoId: String, codigo: String) {
        context.ajustes.edit { prefs ->
            val actuales = (prefs[claveRecuperacion] ?: "")
                .split(";")
                .filter { it.isNotBlank() && !it.startsWith("$remotoId=") }
            prefs[claveRecuperacion] = (actuales + "$remotoId=$codigo").joinToString(";")
        }
    }

    suspend fun recuperacionDe(remotoId: String): String? =
        (context.ajustes.data.first()[claveRecuperacion] ?: "")
            .split(";")
            .firstOrNull { it.startsWith("$remotoId=") }
            ?.substringAfter("=")
            ?.takeIf { it.isNotBlank() }
}
