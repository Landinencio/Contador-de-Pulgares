package com.pulgares.app.data

import com.pulgares.app.data.local.BaseDatos
import com.pulgares.app.data.local.aDominio
import com.pulgares.app.data.local.aEntidad
import com.pulgares.app.data.red.ClienteNube
import com.pulgares.app.data.red.IdentidadMovil
import com.pulgares.app.data.red.Sincronizador
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Grupo
import com.pulgares.app.domain.model.Pago
import org.json.JSONArray
import org.json.JSONObject

/**
 * Todo lo que tiene que ver con compartir un grupo entre móviles. Vive aparte del
 * [Repositorio] local a propósito: la app funciona entera sin esto, y así se ve de
 * un vistazo qué parte depende de la nube y qué parte no.
 */
class RepositorioNube(
    private val bd: BaseDatos,
    private val local: Repositorio,
    private val identidad: IdentidadMovil,
    private val cliente: ClienteNube = ClienteNube()
) {

    val disponible: Boolean get() = cliente.disponible

    /** Lo que se le cuenta al usuario después de sincronizar. */
    data class Resultado(
        val codigo: String,
        val gastosNuevos: Int,
        val pagosNuevos: Int,
        /** Peticiones de entrar esperando al dueño (vacío si no soy yo). */
        val solicitudes: List<Sincronizador.Solicitud> = emptyList(),
        /** Colegas creados a mano sin móvil, para asignarlos al aprobar. */
        val colegasLibres: List<Colega> = emptyList(),
        /** Zumbidos que llegaron en esta sincronización (ya consumidos en la nube). */
        val zumbidos: List<Sincronizador.Zumbido> = emptyList(),
        /** La votación del nombre en marcha, si hay alguna. */
        val votacion: Sincronizador.VotacionNombre? = null,
        /** Cómo acabó la última votación (o el último cambio directo). */
        val avisoNombre: Sincronizador.AvisoNombre? = null,
        /** ¿Me queda el cambio de nombre gratis? */
        val meQuedaCambioGratis: Boolean = true,
        /** Colegas que han cambiado de nombre en esta bajada: (antes, ahora). */
        val renombrados: List<Pair<String, String>> = emptyList()
    )

    /**
     * Sube este grupo a la nube por primera vez y devuelve su código. Los colegas,
     * gastos y pagos que ya había se suben en la misma tanda.
     */
    suspend fun comparte(grupoId: String): Resultado {
        val estado = estadoLocal(grupoId)
        val yo = estado.grupo.yo
            ?: throw ClienteNube.ErrorNube(0, "Este grupo no tiene marcado quién eres tú")
        val uid = identidad.uid()

        val creado = cliente.crearGrupo(
            uid,
            JSONObject()
                .put("nombre", estado.grupo.nombre)
                .put("emoji", estado.grupo.emoji)
                .put("miColegaId", yo.id)
                .put("colegas", JSONArray().apply {
                    estado.grupo.colegas.forEach { colega ->
                        put(
                            JSONObject()
                                .put("id", colega.id)
                                .put("nombre", colega.nombre)
                                .put("avatar", colega.avatar ?: JSONObject.NULL)
                                .put("activo", colega.activo)
                        )
                    }
                })
        )

        val remotoId = creado.getString("grupoId")
        val codigo = creado.getString("codigo")
        identidad.guardaRecuperacion(remotoId, creado.getString("codigoRecuperacion"))

        bd.grupos().guardaGrupo(
            estado.grupo.copy(
                codigo = codigo,
                remotoId = remotoId,
                version = creado.optLong("version", System.currentTimeMillis())
            ).aEntidad()
        )

        // Y de paso sube lo que ya hubiera apuntado.
        val subida = sincroniza(grupoId)
        return Resultado(codigo, subida.gastosNuevos, subida.pagosNuevos)
    }

    /**
     * Sube lo de este móvil, baja lo de los demás y funde. Devuelve cuántas cosas
     * nuevas han llegado, que es lo que le interesa al usuario.
     */
    suspend fun sincroniza(grupoId: String): Resultado {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId
            ?: throw ClienteNube.ErrorNube(0, "Este grupo todavía no se ha compartido")
        val uid = identidad.uid()

        val respuesta = cliente.sube(
            uid,
            Sincronizador.paqueteDeSubida(
                grupoId = grupoId,
                remotoId = remotoId,
                colegas = estado.grupo.colegas,
                gastos = estado.gastos,
                pagos = estado.pagos
            )
        )

        val remoto = Sincronizador.leeGrupo(respuesta.getJSONObject("grupo"), grupoId)

        // El nombre y el emoji del grupo no viajan en la subida normal: tienen su
        // propia ruta. Si esa llamada falló (sin cobertura al renombrar), el
        // cambio se quedaba en este móvil para siempre, porque las
        // sincronizaciones siguientes solo bajan. Aquí se reintenta: si lo de
        // aquí es más nuevo que lo de la nube, se empuja por la ruta legítima.
        if (estado.grupo.version > remoto.version) {
            runCatching { editaGrupo(grupoId) }
        }

        return guardaLoQueLlega(estado, remoto)
    }

    /** Cómo quedó una petición de entrar. */
    sealed interface Solicitud {
        /** Aprobada: el grupo ya está bajado en este móvil. */
        data class Dentro(val grupoId: String) : Solicitud

        /** El dueño todavía no ha dicho nada. */
        data class Pendiente(val nombreGrupo: String, val emoji: String) : Solicitud

        /** El dueño dijo que no (se puede volver a pedir). */
        data class Rechazada(val nombreGrupo: String) : Solicitud
    }

    /**
     * Pide entrar en un grupo con su código, llevando el nombre y el monigote
     * del perfil. Idempotente: llamarla otra vez pregunta cómo va la cosa, y si
     * ya está aprobada, baja el grupo y lo deja guardado.
     */
    suspend fun solicita(codigo: String): Solicitud {
        val perfil = identidad.perfil()
            ?: throw ClienteNube.ErrorNube(0, "Primero ponte un nombre en tu perfil")
        val uid = identidad.uid()
        val respuesta = cliente.unirse(uid, codigo, perfil.nombre, perfil.avatar)
        val grupoJson = respuesta.getJSONObject("grupo")

        return when (respuesta.optString("estado")) {
            "dentro" -> {
                identidad.quitaPendiente(codigo)
                Solicitud.Dentro(guardaGrupoEntero(grupoJson))
            }

            "rechazada" -> {
                identidad.quitaPendiente(codigo)
                Solicitud.Rechazada(grupoJson.optString("nombre"))
            }

            else -> {
                val pendiente = IdentidadMovil.Pendiente(
                    codigo = codigo,
                    nombreGrupo = grupoJson.optString("nombre"),
                    emoji = grupoJson.optString("emoji").ifBlank { "👥" }
                )
                identidad.guardaPendiente(pendiente)
                Solicitud.Pendiente(pendiente.nombreGrupo, pendiente.emoji)
            }
        }
    }

    /** Baja un grupo entero de la nube y lo guarda (o actualiza) en este móvil. */
    private suspend fun guardaGrupoEntero(grupoJson: org.json.JSONObject): String {
        val remoto = Sincronizador.leeGrupo(grupoJson, "")

        // ¿Ya tenía este grupo bajado? Entonces se actualiza en vez de duplicarlo.
        val existente = bd.grupos().grupoPorRemoto(remoto.remotoId)
        val grupoId = existente?.id ?: Repositorio.nuevoId()

        bd.grupos().guardaGrupo(
            Grupo(
                id = grupoId,
                nombre = remoto.nombre,
                emoji = remoto.emoji,
                creadoMillis = existente?.creadoMillis ?: System.currentTimeMillis(),
                codigo = remoto.codigo,
                remotoId = remoto.remotoId,
                version = remoto.version
            ).aEntidad()
        )
        local.guardaColegas(grupoId, remoto.colegas)

        val conGrupo = remoto.copy(
            gastos = remoto.gastos.map { it.copy(grupoId = grupoId) },
            pagos = remoto.pagos.map { it.copy(grupoId = grupoId) }
        )
        guardaLoQueLlega(estadoLocal(grupoId), conGrupo)
        return grupoId
    }

    /** Las peticiones de este móvil que siguen en el aire. */
    fun observaPendientes() = identidad.observaPendientes()

    /**
     * Pregunta por todas las solicitudes pendientes. Devuelve el resultado de
     * cada una para que la portada cuente qué ha pasado.
     */
    suspend fun compruebaPendientes(): List<Pair<IdentidadMovil.Pendiente, Solicitud>> =
        identidad.pendientes().map { pendiente ->
            pendiente to runCatching { solicita(pendiente.codigo) }
                .getOrElse { Solicitud.Pendiente(pendiente.nombreGrupo, pendiente.emoji) }
        }

    /** El dueño aprueba una solicitud; [colegaId] la asigna a uno creado a mano. */
    suspend fun aprueba(grupoId: String, solicitanteUid: String, colegaId: String?): Sincronizador.GrupoRemoto {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId
            ?: throw ClienteNube.ErrorNube(0, "Este grupo no está compartido")
        val respuesta = cliente.aprueba(identidad.uid(), remotoId, solicitanteUid, colegaId)
        val remoto = Sincronizador.leeGrupo(respuesta.getJSONObject("grupo"), grupoId)
        guardaLoQueLlega(estado, remoto)
        return remoto
    }

    suspend fun rechaza(grupoId: String, solicitanteUid: String): Sincronizador.GrupoRemoto {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId
            ?: throw ClienteNube.ErrorNube(0, "Este grupo no está compartido")
        val respuesta = cliente.rechaza(identidad.uid(), remotoId, solicitanteUid)
        val remoto = Sincronizador.leeGrupo(respuesta.getJSONObject("grupo"), grupoId)
        guardaLoQueLlega(estado, remoto)
        return remoto
    }

    /** Empuja el nombre y el emoji del grupo a la nube (cualquier miembro). */
    suspend fun editaGrupo(grupoId: String): Sincronizador.GrupoRemoto {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId
            ?: throw ClienteNube.ErrorNube(0, "Este grupo no está compartido")
        val respuesta = cliente.editaGrupo(
            identidad.uid(),
            org.json.JSONObject()
                .put("grupoId", remotoId)
                .put("nombre", estado.grupo.nombre)
                .put("emoji", estado.grupo.emoji)
                .put("version", estado.grupo.version)
        )
        val remoto = Sincronizador.leeGrupo(respuesta.getJSONObject("grupo"), grupoId)
        guardaLoQueLlega(estado, remoto)
        return remoto
    }

    /**
     * Pide cambiar el nombre del grupo. Devuelve true si ya está hecho (le
     * quedaba el cambio gratis, o no hay nadie más con móvil a quien preguntar) y
     * false si ha quedado una votación abierta.
     */
    suspend fun proponeNombre(grupoId: String, nombre: String, emoji: String): Boolean {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId
            ?: throw ClienteNube.ErrorNube(0, "Este grupo no está compartido")
        val respuesta = cliente.proponeNombre(
            identidad.uid(),
            remotoId,
            nombre,
            emoji,
            estado.grupo.yo?.nombre ?: "Alguien"
        )
        val remoto = Sincronizador.leeGrupo(respuesta.getJSONObject("grupo"), grupoId)
        guardaLoQueLlega(estado, remoto)
        return respuesta.optBoolean("aplicado", false)
    }

    /**
     * Vota la propuesta abierta. Devuelve null si aún faltan votos, y true/false
     * cuando ya se ha resuelto (aprobada o rechazada).
     */
    suspend fun votaNombre(grupoId: String, aFavor: Boolean): Boolean? {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId
            ?: throw ClienteNube.ErrorNube(0, "Este grupo no está compartido")
        val respuesta = cliente.votaNombre(identidad.uid(), remotoId, aFavor)
        if (!respuesta.optBoolean("resuelta", false)) return null
        // Al resolverse vuelve el grupo entero: hay que aplicar el nombre nuevo.
        respuesta.optJSONObject("grupo")?.let { grupo ->
            guardaLoQueLlega(estado, Sincronizador.leeGrupo(grupo, grupoId))
        }
        return respuesta.optBoolean("aprobada", false)
    }

    /** Manda un zumbido a un colega del grupo. Devuelve las veces acumuladas. */
    suspend fun zumba(grupoId: String, aColegaId: String): Int {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId
            ?: throw ClienteNube.ErrorNube(0, "Este grupo no está compartido")
        return cliente.zumba(identidad.uid(), remotoId, aColegaId).optInt("veces", 1)
    }

    /** Cambia el código del grupo (solo el dueño). */
    suspend fun rotaCodigo(grupoId: String): String {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId
            ?: throw ClienteNube.ErrorNube(0, "Este grupo no está compartido")
        val nuevo = cliente.rotaCodigo(identidad.uid(), remotoId).getString("codigo")
        bd.grupos().guardaGrupo(estado.grupo.copy(codigo = nuevo).aEntidad())
        return nuevo
    }

    /** El código de recuperación de un grupo que creó este móvil. */
    suspend fun codigoDeRecuperacion(grupoId: String): String? {
        val remotoId = estadoLocal(grupoId).grupo.remotoId ?: return null
        return identidad.recuperacionDe(remotoId)
    }

    /** Deja de compartir en este móvil: el grupo se queda local. */
    suspend fun dejaDeCompartir(grupoId: String) {
        val estado = estadoLocal(grupoId)
        val remotoId = estado.grupo.remotoId ?: return
        runCatching { cliente.salir(identidad.uid(), remotoId) }
        bd.grupos().guardaGrupo(estado.grupo.copy(codigo = null, remotoId = null).aEntidad())
    }

    // ------------------------------------------------------------- fontanería

    private suspend fun estadoLocal(grupoId: String): EstadoGrupo =
        local.grupoDeUnaVez(grupoId)
            ?: throw ClienteNube.ErrorNube(0, "Ese grupo no está en este móvil")

    /** Guarda lo que llega de la nube fundiéndolo con lo de aquí. */
    private suspend fun guardaLoQueLlega(
        estado: EstadoGrupo,
        remoto: Sincronizador.GrupoRemoto
    ): Resultado {
        val grupoId = estado.grupo.id

        // Los colegas se fusionan igual que los gastos: gana la version mas alta.
        // Antes la nube machacaba lo local sin mirar, asi que un nombre recien
        // cambiado en este movil volvia al de antes en cuanto otro sincronizaba.
        val miColega = remoto.miColegaId ?: estado.grupo.yo?.id
        val colegas = Sincronizador.fusiona(
            locales = estado.grupo.colegas,
            remotos = remoto.colegas,
            id = Colega::id,
            version = Colega::version
        ).map { it.copy(soyYo = it.id == miColega) }
        local.guardaColegas(grupoId, colegas.ifEmpty { estado.grupo.colegas })

        // Los nombres que han cambiado al bajar, para poder cantarlos: cambiar en
        // silencio era media queja de los testers ("no da la sensacion de que se
        // haya hecho"). Lo que cambia por MI mano no entra aqui: en local ya
        // estaba puesto antes de sincronizar, asi que no hay diferencia.
        val comoEstaban = estado.grupo.colegas.associate { it.id to it.nombre }
        val renombrados = colegas.mapNotNull { colega ->
            val antes = comoEstaban[colega.id]
            if (antes != null && antes != colega.nombre) antes to colega.nombre else null
        }

        val gastosAntes = estado.gastos.map { it.id }.toSet()
        val pagosAntes = estado.pagos.map { it.id }.toSet()

        val gastos = Sincronizador.fusiona(
            locales = estado.gastos,
            remotos = remoto.gastos.map { it.copy(grupoId = grupoId) },
            id = Gasto::id,
            version = Gasto::version
        )
        val pagos = Sincronizador.fusiona(
            locales = estado.pagos,
            remotos = remoto.pagos.map { it.copy(grupoId = grupoId) },
            id = Pago::id,
            version = Pago::version
        )

        gastos.forEach { bd.gastos().guarda(it.aEntidad()) }
        pagos.forEach { bd.pagos().guarda(it.aEntidad()) }

        // El nombre del grupo: gana la version mas alta.
        if (remoto.version > estado.grupo.version) {
            bd.grupos().guardaGrupo(
                estado.grupo.copy(
                    nombre = remoto.nombre,
                    emoji = remoto.emoji,
                    codigo = remoto.codigo,
                    version = remoto.version
                ).aEntidad()
            )
        }

        return Resultado(
            codigo = remoto.codigo,
            gastosNuevos = gastos.count { it.id !in gastosAntes },
            pagosNuevos = pagos.count { it.id !in pagosAntes },
            solicitudes = remoto.solicitudes,
            colegasLibres = remoto.colegas.filter { it.id in remoto.colegasLibres },
            zumbidos = remoto.zumbidos,
            votacion = remoto.votacion,
            avisoNombre = remoto.avisoNombre,
            meQuedaCambioGratis = remoto.meQuedaCambioGratis,
            renombrados = renombrados
        )
    }

}
