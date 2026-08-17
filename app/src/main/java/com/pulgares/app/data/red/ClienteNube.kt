package com.pulgares.app.data.red

import com.pulgares.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente del backend de sincronización (Lambda + DynamoDB en la cuenta AWS
 * personal). HttpURLConnection y org.json a pelo: son dos llamadas contadas y no
 * merece la pena arrastrar Retrofit para esto.
 *
 * Si no hay token configurado, [disponible] es false y la app ni lo intenta: se
 * queda como estaba, 100% local. Es lo que pasa en cualquier build sin el secreto
 * de CI, así que la app tiene que funcionar igual sin esto.
 */
class ClienteNube(
    private val base: String = BuildConfig.SYNC_URL.removeSuffix("/"),
    private val token: String = BuildConfig.SYNC_TOKEN
) {

    val disponible: Boolean get() = token.isNotBlank() && base.startsWith("http")

    /** Un fallo que se le puede contar al usuario tal cual. */
    class ErrorNube(val codigo: Int, mensaje: String) : Exception(mensaje)

    suspend fun crearGrupo(uid: String, cuerpo: JSONObject): JSONObject =
        post(uid, "/pulgares/crear", cuerpo)

    /**
     * Entra en un grupo con su código. Sin `colegaId` ni `nombre`, el backend
     * devuelve el grupo y la lista de colegas que nadie ha reclamado, para que la
     * app pregunte "¿quién eres?" en vez de crear un duplicado.
     */
    suspend fun unirse(uid: String, codigo: String, colegaId: String? = null, nombre: String? = null): JSONObject {
        val cuerpo = JSONObject().put("codigo", codigo)
        if (colegaId != null) cuerpo.put("colegaId", colegaId)
        if (nombre != null) cuerpo.put("nombre", nombre)
        return post(uid, "/pulgares/unirse", cuerpo)
    }

    suspend fun sube(uid: String, cuerpo: JSONObject): JSONObject =
        post(uid, "/pulgares/sube", cuerpo)

    suspend fun editaGrupo(uid: String, cuerpo: JSONObject): JSONObject =
        post(uid, "/pulgares/editar", cuerpo)

    suspend fun rotaCodigo(uid: String, grupoId: String): JSONObject =
        post(uid, "/pulgares/rotar", JSONObject().put("grupoId", grupoId))

    suspend fun recupera(uid: String, codigo: String, recuperacion: String): JSONObject =
        post(
            uid,
            "/pulgares/recuperar",
            JSONObject().put("codigo", codigo).put("recuperacion", recuperacion)
        )

    suspend fun salir(uid: String, grupoId: String): JSONObject =
        post(uid, "/pulgares/salir", JSONObject().put("grupoId", grupoId))

    suspend fun misGrupos(uid: String): JSONArray =
        get(uid, "/pulgares/mios").optJSONArray("grupos") ?: JSONArray()

    suspend fun grupo(uid: String, grupoId: String): JSONObject =
        get(uid, "/pulgares/grupo?id=$grupoId").getJSONObject("grupo")

    // ---------------------------------------------------------------- fontanería

    private suspend fun post(uid: String, ruta: String, cuerpo: JSONObject): JSONObject =
        llamada(uid, ruta, "POST", cuerpo)

    private suspend fun get(uid: String, ruta: String): JSONObject =
        llamada(uid, ruta, "GET", null)

    private suspend fun llamada(
        uid: String,
        ruta: String,
        metodo: String,
        cuerpo: JSONObject?
    ): JSONObject = withContext(Dispatchers.IO) {
        if (!disponible) {
            throw ErrorNube(0, "La sincronización no está configurada en esta versión")
        }

        val conexion = (URL("$base$ruta").openConnection() as HttpURLConnection).apply {
            requestMethod = metodo
            // El content-type no es opcional: sin él, API Gateway manda el cuerpo
            // en base64 y el backend tiene que adivinarlo.
            setRequestProperty("content-type", "application/json; charset=utf-8")
            setRequestProperty("x-token", token)
            setRequestProperty("x-pulgares-uid", uid)
            connectTimeout = 15_000
            readTimeout = 30_000
            doInput = true
        }

        try {
            if (cuerpo != null) {
                conexion.doOutput = true
                conexion.outputStream.use { it.write(cuerpo.toString().toByteArray()) }
            }

            val codigo = conexion.responseCode
            val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
            val texto = flujo?.bufferedReader()?.use(BufferedReader::readText).orEmpty()

            val json = if (texto.isBlank()) JSONObject() else JSONObject(texto)
            if (codigo !in 200..299) {
                throw ErrorNube(
                    codigo,
                    json.optString("error").ifBlank { "El servidor ha dicho $codigo" }
                )
            }
            json
        } catch (error: ErrorNube) {
            throw error
        } catch (error: Exception) {
            // Sin internet, DNS caído, timeout... nada de tecnicismos al usuario.
            throw ErrorNube(0, "No se ha podido hablar con la nube. ¿Tienes cobertura?")
        } finally {
            conexion.disconnect()
        }
    }
}
