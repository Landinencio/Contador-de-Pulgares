package com.pulgares.app.data.red

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    private val claveCobrador = booleanPreferencesKey("cobradorContratado")
    private val claveUltimoAviso = longPreferencesKey("cobradorUltimoAviso")
    private val claveNombre = stringPreferencesKey("perfilNombre")
    private val claveAvatar = stringPreferencesKey("perfilAvatar")
    private val clavePendientes = stringPreferencesKey("solicitudesPendientes")

    // ---- el perfil: quién soy en todos los grupos ----

    /** Nombre y monigote que viajan con cada solicitud de entrar a un grupo. */
    data class Perfil(val nombre: String, val avatar: String)

    /** null = primer arranque: hay que pasar por la pantalla de perfil. */
    suspend fun perfil(): Perfil? {
        val datos = context.ajustes.data.first()
        val nombre = datos[claveNombre]?.takeIf { it.isNotBlank() } ?: return null
        return Perfil(nombre, datos[claveAvatar].orEmpty())
    }

    suspend fun guardaPerfil(nombre: String, avatar: String) {
        context.ajustes.edit {
            it[claveNombre] = nombre.trim()
            it[claveAvatar] = avatar
        }
    }

    /** Observable, para que la cabecera se entere al cambiar el perfil. */
    fun observaPerfil(): Flow<Perfil?> = context.ajustes.data.map { datos ->
        datos[claveNombre]?.takeIf { it.isNotBlank() }?.let { nombre ->
            Perfil(nombre, datos[claveAvatar].orEmpty())
        }
    }

    // ---- solicitudes que este móvil tiene en el aire ----

    /** Una petición de entrar que aún no han aprobado. */
    data class Pendiente(val codigo: String, val nombreGrupo: String, val emoji: String)

    /** Se guardan como "codigo|nombre|emoji;..." — son una o dos, no una tabla. */
    suspend fun pendientes(): List<Pendiente> =
        (context.ajustes.data.first()[clavePendientes] ?: "")
            .split(";")
            .mapNotNull { crudo ->
                val trozos = crudo.split("|")
                if (trozos.size < 3 || trozos[0].isBlank()) return@mapNotNull null
                Pendiente(trozos[0], trozos[1], trozos[2])
            }

    fun observaPendientes(): Flow<List<Pendiente>> = context.ajustes.data.map { datos ->
        (datos[clavePendientes] ?: "")
            .split(";")
            .mapNotNull { crudo ->
                val trozos = crudo.split("|")
                if (trozos.size < 3 || trozos[0].isBlank()) return@mapNotNull null
                Pendiente(trozos[0], trozos[1], trozos[2])
            }
    }

    suspend fun guardaPendiente(pendiente: Pendiente) {
        context.ajustes.edit { prefs ->
            val otras = (prefs[clavePendientes] ?: "")
                .split(";")
                .filter { it.isNotBlank() && !it.startsWith("${pendiente.codigo}|") }
            prefs[clavePendientes] =
                (otras + "${pendiente.codigo}|${pendiente.nombreGrupo}|${pendiente.emoji}")
                    .joinToString(";")
        }
    }

    suspend fun quitaPendiente(codigo: String) {
        context.ajustes.edit { prefs ->
            prefs[clavePendientes] = (prefs[clavePendientes] ?: "")
                .split(";")
                .filter { it.isNotBlank() && !it.startsWith("$codigo|") }
                .joinToString(";")
        }
    }

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

    // ---- el contrato del Cobrador del Frac ----

    suspend fun cobradorContratado(): Boolean =
        context.ajustes.data.first()[claveCobrador] ?: false

    suspend fun contrataCobrador(contratado: Boolean) {
        context.ajustes.edit { it[claveCobrador] = contratado }
    }

    suspend fun ultimoAvisoCobrador(): Long =
        context.ajustes.data.first()[claveUltimoAviso] ?: 0L

    suspend fun marcaAvisoCobrador(millis: Long) {
        context.ajustes.edit { it[claveUltimoAviso] = millis }
    }

    suspend fun recuperacionDe(remotoId: String): String? =
        (context.ajustes.data.first()[claveRecuperacion] ?: "")
            .split(";")
            .firstOrNull { it.startsWith("$remotoId=") }
            ?.substringAfter("=")
            ?.takeIf { it.isNotBlank() }
}
