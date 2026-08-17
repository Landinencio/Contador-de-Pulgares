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
        val pagosNuevos: Int
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
        return guardaLoQueLlega(estado, remoto)
    }

    /** Entra en un grupo que ya existe. Devuelve null si hay que elegir quién eres. */
    suspend fun mira(codigo: String): Sincronizador.GrupoRemoto {
        val uid = identidad.uid()
        val respuesta = cliente.unirse(uid, codigo)
        // El id local todavía no existe: se usa un hueco y se rellena al entrar.
        return Sincronizador.leeGrupo(respuesta.getJSONObject("grupo"), "")
    }

    /**
     * Se une de verdad, reclamando un colega existente o creándose uno nuevo, y
     * deja el grupo guardado en este móvil.
     */
    suspend fun entra(codigo: String, colegaId: String?, miNombre: String?): String {
        val uid = identidad.uid()
        val respuesta = cliente.unirse(uid, codigo, colegaId, miNombre)
        val remoto = Sincronizador.leeGrupo(respuesta.getJSONObject("grupo"), "")

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

        // Los colegas: la nube manda, pero sin perder quién soy yo en este móvil.
        val miColega = remoto.miColegaId ?: estado.grupo.yo?.id
        local.guardaColegas(
            grupoId,
            remoto.colegas.map { it.copy(soyYo = it.id == miColega) }
                .ifEmpty { estado.grupo.colegas }
        )

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
            pagosNuevos = pagos.count { it.id !in pagosAntes }
        )
    }

    /** Los colegas de un grupo remoto, para la pantalla de "¿quién eres?". */
    fun colegasParaElegir(remoto: Sincronizador.GrupoRemoto): List<Colega> =
        remoto.colegas.filter { it.id in remoto.colegasLibres }
}
