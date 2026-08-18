package com.pulgares.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.data.EstadoGrupo
import com.pulgares.app.data.Repositorio
import com.pulgares.app.data.RepositorioNube
import com.pulgares.app.data.red.ClienteNube
import com.pulgares.app.data.red.IdentidadMovil
import com.pulgares.app.data.ResumenGrupo
import com.pulgares.app.domain.model.Categoria
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Reparto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Un unico ViewModel para toda la app: son cuatro pantallas y comparten estado
 * (los grupos y el avatar propio), asi que separarlo solo anadiria ceremonia.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PulgaresViewModel(
    private val repo: Repositorio,
    private val identidad: IdentidadMovil? = null,
    private val nube: RepositorioNube? = null
) : ViewModel() {

    // ---- el perfil: quién soy en todos los grupos ----

    /** null mientras carga; Perfil("") no existe: sin nombre no hay perfil. */
    private val _perfilCargado = MutableStateFlow(false)
    val perfilCargado: StateFlow<Boolean> = _perfilCargado

    val perfil: StateFlow<IdentidadMovil.Perfil?> =
        (identidad?.observaPerfil() ?: flowOf(null))
            .onEach { _perfilCargado.value = true }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Sugerencia para el primer arranque de quien YA usaba la app: el nombre y
     * monigote de su colega "yo" más reciente, para no hacerle escribirlo otra vez.
     */
    suspend fun perfilSugerido(): IdentidadMovil.Perfil? = repo.miYoMasReciente()?.let {
        IdentidadMovil.Perfil(it.nombre, it.avatar.orEmpty())
    }

    fun guardaPerfil(nombre: String, avatar: Monigote, luego: () -> Unit = {}) {
        viewModelScope.launch {
            identidad?.guardaPerfil(nombre, avatar.serializa())
            // El monigote del perfil es el mismo en todos los grupos.
            repo.guardaMiAvatar(avatar.serializa())
            repo.renombraMisYo(nombre)
            sincronizaTodosLosCompartidos()
            luego()
        }
    }

    // ---- solicitudes pendientes de este móvil (portada) ----

    val pendientes: StateFlow<List<IdentidadMovil.Pendiente>> =
        (nube?.observaPendientes() ?: flowOf(emptyList()))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- solicitudes que me llegan como dueño (por grupo) ----

    private val _solicitudes = MutableStateFlow<Map<String, RepositorioNube.Resultado>>(emptyMap())

    /** El último resultado de sync por grupo: solicitudes y colegas libres. */
    val solicitudes: StateFlow<Map<String, RepositorioNube.Resultado>> = _solicitudes

    private fun apuntaResultado(grupoId: String, resultado: RepositorioNube.Resultado) {
        _solicitudes.value = _solicitudes.value + (grupoId to resultado)
    }

    /** ¿Esta build lleva sincronización? Sin token, la app es 100% local. */
    val nubeDisponible: Boolean get() = nube?.disponible == true

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando

    /**
     * Envuelve una operación de red: enciende el indicador, y si algo falla lo
     * cuenta en castellano en vez de dejar la pantalla colgada.
     */
    private fun enLaNube(
        onError: (String) -> Unit,
        bloque: suspend (RepositorioNube) -> Unit
    ) {
        val destino = nube
        if (destino == null || !destino.disponible) {
            onError("Esta versión de la app no lleva sincronización")
            return
        }
        viewModelScope.launch {
            _sincronizando.value = true
            try {
                bloque(destino)
            } catch (error: ClienteNube.ErrorNube) {
                onError(error.message ?: "No ha salido bien")
            } catch (error: Exception) {
                onError("Algo ha ido mal al hablar con la nube")
            } finally {
                _sincronizando.value = false
            }
        }
    }

    // ---- el sync automático: la razón de que los gastos crucen solos ----

    private val syncsPendientes = mutableMapOf<String, Job>()

    /**
     * Sincroniza un grupo compartido después de un cambio, con a un pequeño
     * respiro para que una racha (tres votos seguidos) sea UNA subida y no tres.
     *
     * Este es el arreglo del "mi compañero no ve lo que yo subo": antes solo se
     * sincronizaba al abrir el grupo, así que todo lo apuntado ya dentro se
     * quedaba en el móvil hasta darle a "Sincronizar ahora" a mano.
     */
    private fun sincronizaTrasCambio(grupoId: String?) {
        if (grupoId == null) return
        val destino = nube ?: return
        if (!destino.disponible) return
        syncsPendientes[grupoId]?.cancel()
        syncsPendientes[grupoId] = viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            val compartido = repo.grupoDeUnaVez(grupoId)?.grupo?.compartido == true
            if (!compartido) return@launch
            runCatching { destino.sincroniza(grupoId) }
                .onSuccess { apuntaResultado(grupoId, it) }
        }
    }

    /** Al arrancar la app: bajar lo de los demás en todos los grupos compartidos. */
    fun sincronizaTodosLosCompartidos() {
        val destino = nube ?: return
        if (!destino.disponible) return
        viewModelScope.launch {
            repo.idsDeGruposCompartidos().forEach { grupoId ->
                runCatching { destino.sincroniza(grupoId) }
                    .onSuccess { apuntaResultado(grupoId, it) }
            }
        }
    }

    /** Sube el grupo por primera vez y devuelve su código de invitación. */
    fun comparte(grupoId: String, onHecho: (String) -> Unit, onError: (String) -> Unit) {
        enLaNube(onError) { destino ->
            val resultado = destino.comparte(grupoId)
            onHecho(resultado.codigo)
        }
    }

    fun sincroniza(grupoId: String, onHecho: (RepositorioNube.Resultado) -> Unit, onError: (String) -> Unit) {
        enLaNube(onError) { destino ->
            val resultado = destino.sincroniza(grupoId)
            apuntaResultado(grupoId, resultado)
            onHecho(resultado)
        }
    }

    /** Sync de fondo al abrir un grupo compartido: sin ruido si no hay nada. */
    fun sincronizaEnSilencio(grupoId: String, onNovedades: (RepositorioNube.Resultado) -> Unit) {
        val destino = nube ?: return
        if (!destino.disponible) return
        viewModelScope.launch {
            runCatching { destino.sincroniza(grupoId) }.onSuccess { resultado ->
                apuntaResultado(grupoId, resultado)
                if (resultado.gastosNuevos > 0 || resultado.pagosNuevos > 0 ||
                    resultado.solicitudes.isNotEmpty()
                ) {
                    onNovedades(resultado)
                }
            }
        }
    }

    /** Pide entrar en un grupo con el código (o pregunta cómo va la petición). */
    fun solicita(codigo: String, onHecho: (RepositorioNube.Solicitud) -> Unit, onError: (String) -> Unit) {
        enLaNube(onError) { destino -> onHecho(destino.solicita(codigo)) }
    }

    /** Repasa todas las peticiones en el aire (el botón de la portada). */
    fun compruebaPendientes(
        onHecho: (List<Pair<IdentidadMovil.Pendiente, RepositorioNube.Solicitud>>) -> Unit,
        onError: (String) -> Unit
    ) {
        enLaNube(onError) { destino -> onHecho(destino.compruebaPendientes()) }
    }

    fun apruebaSolicitud(
        grupoId: String,
        solicitanteUid: String,
        colegaId: String?,
        onHecho: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        enLaNube(onError) { destino ->
            val remoto = destino.aprueba(grupoId, solicitanteUid, colegaId)
            apuntaResultado(
                grupoId,
                RepositorioNube.Resultado(
                    codigo = remoto.codigo,
                    gastosNuevos = 0,
                    pagosNuevos = 0,
                    solicitudes = remoto.solicitudes,
                    colegasLibres = remoto.colegas.filter { it.id in remoto.colegasLibres }
                )
            )
            onHecho(remoto.solicitudes.size.toString())
        }
    }

    fun rechazaSolicitud(grupoId: String, solicitanteUid: String, onHecho: () -> Unit, onError: (String) -> Unit) {
        enLaNube(onError) { destino ->
            val remoto = destino.rechaza(grupoId, solicitanteUid)
            apuntaResultado(
                grupoId,
                RepositorioNube.Resultado(
                    codigo = remoto.codigo,
                    gastosNuevos = 0,
                    pagosNuevos = 0,
                    solicitudes = remoto.solicitudes,
                    colegasLibres = remoto.colegas.filter { it.id in remoto.colegasLibres }
                )
            )
            onHecho()
        }
    }

    fun rotaCodigo(grupoId: String, onHecho: (String) -> Unit, onError: (String) -> Unit) {
        enLaNube(onError) { destino -> onHecho(destino.rotaCodigo(grupoId)) }
    }

    fun dejaDeCompartir(grupoId: String, onHecho: () -> Unit, onError: (String) -> Unit) {
        enLaNube(onError) { destino ->
            destino.dejaDeCompartir(grupoId)
            onHecho()
        }
    }

    fun codigoDeRecuperacion(grupoId: String, onHecho: (String?) -> Unit) {
        viewModelScope.launch { onHecho(nube?.codigoDeRecuperacion(grupoId)) }
    }

    val grupos: StateFlow<List<ResumenGrupo>> = repo.observaResumenGrupos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val miAvatar: StateFlow<Monigote> = repo.observaMiAvatar()
        .map { bruto -> Monigote.parse(bruto) ?: Monigote.ELMONIGOTE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Monigote.ELMONIGOTE)

    private val grupoAbierto = MutableStateFlow<String?>(null)

    val estadoGrupo: StateFlow<EstadoGrupo?> = grupoAbierto
        .flatMapLatest { id -> if (id == null) flowOf(null) else repo.observaGrupo(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun abreGrupo(grupoId: String) {
        grupoAbierto.value = grupoId
    }

    fun creaGrupo(nombre: String, emoji: String, luego: (String) -> Unit) {
        viewModelScope.launch {
            val quien = identidad?.perfil()
            val id = repo.creaGrupo(
                nombre = nombre,
                emoji = emoji,
                miNombre = quien?.nombre ?: "Yo",
                miAvatar = quien?.avatar?.takeIf { it.isNotBlank() }
            )
            grupoAbierto.value = id
            luego(id)
        }
    }

    fun borraGrupo(grupoId: String) {
        viewModelScope.launch { repo.borraGrupo(grupoId) }
    }

    fun anadeColega(grupoId: String, nombre: String, orden: Int) {
        viewModelScope.launch {
            repo.anadeColega(grupoId, nombre, orden)
            sincronizaTrasCambio(grupoId)
        }
    }

    fun guardaColegas(grupoId: String, colegas: List<Colega>) {
        viewModelScope.launch {
            repo.guardaColegas(grupoId, colegas)
            sincronizaTrasCambio(grupoId)
        }
    }

    fun renombraGrupo(grupo: com.pulgares.app.domain.model.Grupo, nombre: String, emoji: String) {
        viewModelScope.launch {
            repo.renombraGrupo(grupo, nombre, emoji)
            // El nombre no viaja en la subida normal: tiene su propia ruta.
            if (grupo.compartido && nube?.disponible == true) {
                runCatching { nube.editaGrupo(grupo.id) }
            }
        }
    }

    /**
     * Saca a un colega del grupo. No se borra su ficha: se marca como inactivo,
     * asi sus gastos de antes siguen teniendo nombre y cara, y el reparto de esos
     * gastos no cambia. Se puede readmitir con [readmiteColega].
     */
    fun quitaColega(grupoId: String, colegas: List<Colega>, fuera: Colega) {
        cambiaActivo(grupoId, colegas, fuera, activo = false)
    }

    fun readmiteColega(grupoId: String, colegas: List<Colega>, vuelve: Colega) {
        cambiaActivo(grupoId, colegas, vuelve, activo = true)
    }

    private fun cambiaActivo(grupoId: String, colegas: List<Colega>, quien: Colega, activo: Boolean) {
        viewModelScope.launch {
            repo.guardaColegas(
                grupoId,
                colegas.map { if (it.id == quien.id) it.copy(activo = activo) else it }
            )
            sincronizaTrasCambio(grupoId)
        }
    }

    fun renombraColega(grupoId: String, colegas: List<Colega>, colega: Colega, nombre: String) {
        viewModelScope.launch {
            repo.guardaColegas(
                grupoId,
                colegas.map { if (it.id == colega.id) it.copy(nombre = nombre) else it }
            )
            sincronizaTrasCambio(grupoId)
        }
    }

    /** Deshace el ultimo bizum registrado por error. */
    fun borraPago(pagoId: String) {
        viewModelScope.launch { sincronizaTrasCambio(repo.borraPago(pagoId)) }
    }

    /**
     * Apunta un gasto nuevo o guarda los cambios de uno que ya existe.
     *
     * [original] es el gasto tal y como estaba: de ahi se conservan la fecha y
     * los pulgares. Sin eso, corregir una tilde del concepto borraba los votos
     * del gasto y lo fechaba hoy, lo que ademas reiniciaba la antiguedad de la
     * deuda y sacaba al moroso del salon de la fama.
     */
    fun apuntaGasto(
        grupoId: String,
        concepto: String,
        importeCentimos: Long,
        pagadorId: String,
        categoria: Categoria,
        reparto: Reparto,
        nota: String?,
        original: Gasto? = null
    ) {
        viewModelScope.launch {
            repo.guardaGasto(
                Gasto(
                    id = original?.id ?: Repositorio.nuevoId(),
                    grupoId = grupoId,
                    concepto = concepto.ifBlank { "Sin nombre" },
                    importeCentimos = importeCentimos,
                    pagadorId = pagadorId,
                    fechaMillis = original?.fechaMillis ?: System.currentTimeMillis(),
                    categoria = categoria,
                    reparto = reparto,
                    nota = nota?.ifBlank { null },
                    pulgaresArriba = original?.pulgaresArriba ?: emptySet(),
                    pulgaresAbajo = original?.pulgaresAbajo ?: emptySet(),
                    // La version es el arbitro de la sincronizacion: cada
                    // guardado la sube para que esta edicion gane a las copias
                    // anteriores en los demas moviles.
                    version = System.currentTimeMillis()
                )
            )
            sincronizaTrasCambio(grupoId)
        }
    }

    fun borraGasto(gastoId: String) {
        viewModelScope.launch { sincronizaTrasCambio(repo.borraGasto(gastoId)) }
    }

    fun votaGasto(gastoId: String, colegaId: String, arriba: Boolean) {
        viewModelScope.launch { sincronizaTrasCambio(repo.votaGasto(gastoId, colegaId, arriba)) }
    }

    fun registraPago(grupoId: String, deQuienId: String, aQuienId: String, importe: Long, nota: String? = null) {
        viewModelScope.launch {
            repo.registraPago(grupoId, deQuienId, aQuienId, importe, nota)
            sincronizaTrasCambio(grupoId)
        }
    }

    fun guardaMiAvatar(monigote: Monigote) {
        viewModelScope.launch {
            repo.guardaMiAvatar(monigote.serializa())
            sincronizaTodosLosCompartidos()
        }
    }

    fun guardaAvatarDe(colegaId: String, monigote: Monigote) {
        viewModelScope.launch {
            repo.guardaAvatarDe(colegaId, monigote.serializa())
            sincronizaTodosLosCompartidos()
        }
    }

    class Fabrica(
        private val repo: Repositorio,
        private val identidad: IdentidadMovil? = null,
        private val nube: RepositorioNube? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PulgaresViewModel(repo, identidad, nube) as T
    }
}
