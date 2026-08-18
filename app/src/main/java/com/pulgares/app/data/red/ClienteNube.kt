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
    base: String = BuildConfig.SYNC_URL,
    private val token: String = BuildConfig.SYNC_TOKEN
) {

    /**
     * La base ya normalizada: el origen a secas. Las rutas de esta clase llevan
     * el /pulgares/ delante, asi que si la base tambien lo trae (paso: la URL por
     * defecto del primer dia acababa en /pulgares) las peticiones salian a
     * /pulgares/pulgares/crear y el API Gateway devolvia un 404 que no era de la
     * Lambda. Se recorta aqui para que ni una config vieja lo reintroduzca.
     */
    private val base: String = normaliza(base)

    val disponible: Boolean get() = token.isNotBlank() && base.startsWith("http")

    companion object {
        /** Quita barras y un /pulgares final: las rutas ya lo aportan. */
        fun normaliza(url: String): String =
            url.trim().removeSuffix("/").removeSuffix("/pulgares").removeSuffix("/")
    }

    /** Un fallo que se le puede contar al usuario tal cual. */
    class ErrorNube(val codigo: Int, mensaje: String) : Exception(mensaje)

    suspend fun crearGrupo(uid: String, cuerpo: JSONObject): JSONObject =
        post(uid, "/pulgares/crear", cuerpo)

    /**
     * Pide entrar en un grupo (o pregunta cómo va la petición: es idempotente).
     * El backend responde con estado dentro / pendiente / rechazada.
     */
    suspend fun unirse(uid: String, codigo: String, nombre: String, avatar: String?): JSONObject {
        val cuerpo = JSONObject()
            .put("codigo", codigo)
            .put("nombre", nombre)
        if (!avatar.isNullOrBlank()) cuerpo.put("avatar", avatar)
        return post(uid, "/pulgares/unirse", cuerpo)
    }

    /** El dueño deja entrar a [solicitanteUid]; [colegaId] hereda uno a mano. */
    suspend fun aprueba(uid: String, grupoId: String, solicitanteUid: String, colegaId: String? = null): JSONObject {
        val cuerpo = JSONObject().put("grupoId", grupoId).put("uid", solicitanteUid)
        if (colegaId != null) cuerpo.put("colegaId", colegaId)
        return post(uid, "/pulgares/aprobar", cuerpo)
    }

    suspend fun rechaza(uid: String, grupoId: String, solicitanteUid: String): JSONObject =
        post(uid, "/pulgares/rechazar", JSONObject().put("grupoId", grupoId).put("uid", solicitanteUid))

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
