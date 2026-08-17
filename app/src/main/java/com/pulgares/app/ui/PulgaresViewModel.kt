package com.pulgares.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.data.EstadoGrupo
import com.pulgares.app.data.Repositorio
import com.pulgares.app.data.RepositorioNube
import com.pulgares.app.data.red.ClienteNube
import com.pulgares.app.data.red.Sincronizador
import com.pulgares.app.data.ResumenGrupo
import com.pulgares.app.domain.model.Categoria
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Reparto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Un unico ViewModel para toda la app: son cuatro pantallas y comparten estado
 * (los grupos y el avatar propio), asi que separarlo solo anadiria ceremonia.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PulgaresViewModel(
    private val repo: Repositorio,
    private val nube: RepositorioNube? = null
) : ViewModel() {

    /** ¿Esta build lleva sincronización? Sin token, la app es 100% local. */
    val nubeDisponible: Boolean get() = nube?.disponible == true

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando

    /** Lo que se está mirando antes de unirse a un grupo ajeno. */
    private val _grupoAlQueUnirse = MutableStateFlow<Sincronizador.GrupoRemoto?>(null)
    val grupoAlQueUnirse: StateFlow<Sincronizador.GrupoRemoto?> = _grupoAlQueUnirse

    fun olvidaGrupoAlQueUnirse() {
        _grupoAlQueUnirse.value = null
    }

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

    /** Sube el grupo por primera vez y devuelve su código de invitación. */
    fun comparte(grupoId: String, onHecho: (String) -> Unit, onError: (String) -> Unit) {
        enLaNube(onError) { destino ->
            val resultado = destino.comparte(grupoId)
            onHecho(resultado.codigo)
        }
    }

    fun sincroniza(grupoId: String, onHecho: (RepositorioNube.Resultado) -> Unit, onError: (String) -> Unit) {
        enLaNube(onError) { destino -> onHecho(destino.sincroniza(grupoId)) }
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

    /** Primer paso de unirse: mirar qué hay detrás del código. */
    fun miraCodigo(codigo: String, onError: (String) -> Unit) {
        enLaNube(onError) { destino ->
            _grupoAlQueUnirse.value = destino.mira(codigo)
        }
    }

    /** Segundo paso: entrar como un colega concreto (o como uno nuevo). */
    fun entra(
        codigo: String,
        colegaId: String?,
        miNombre: String?,
        onHecho: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        enLaNube(onError) { destino ->
            val grupoId = destino.entra(codigo, colegaId, miNombre)
            _grupoAlQueUnirse.value = null
            onHecho(grupoId)
        }
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

    fun creaGrupo(nombre: String, emoji: String, colegas: List<String>, miNombre: String, luego: (String) -> Unit) {
        viewModelScope.launch {
            val id = repo.creaGrupo(nombre, emoji, colegas, miNombre)
            grupoAbierto.value = id
            luego(id)
        }
    }

    fun borraGrupo(grupoId: String) {
        viewModelScope.launch { repo.borraGrupo(grupoId) }
    }

    fun anadeColega(grupoId: String, nombre: String, orden: Int) {
        viewModelScope.launch { repo.anadeColega(grupoId, nombre, orden) }
    }

    fun guardaColegas(grupoId: String, colegas: List<Colega>) {
        viewModelScope.launch { repo.guardaColegas(grupoId, colegas) }
    }

    fun renombraGrupo(grupo: com.pulgares.app.domain.model.Grupo, nombre: String, emoji: String) {
        viewModelScope.launch { repo.renombraGrupo(grupo, nombre, emoji) }
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
        }
    }

    fun renombraColega(grupoId: String, colegas: List<Colega>, colega: Colega, nombre: String) {
        viewModelScope.launch {
            repo.guardaColegas(
                grupoId,
                colegas.map { if (it.id == colega.id) it.copy(nombre = nombre) else it }
            )
        }
    }

    /** Deshace el ultimo bizum registrado por error. */
    fun borraPago(pagoId: String) {
        viewModelScope.launch { repo.borraPago(pagoId) }
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
        }
    }

    fun borraGasto(gastoId: String) {
        viewModelScope.launch { repo.borraGasto(gastoId) }
    }

    fun votaGasto(gastoId: String, colegaId: String, arriba: Boolean) {
        viewModelScope.launch { repo.votaGasto(gastoId, colegaId, arriba) }
    }

    fun registraPago(grupoId: String, deQuienId: String, aQuienId: String, importe: Long, nota: String? = null) {
        viewModelScope.launch { repo.registraPago(grupoId, deQuienId, aQuienId, importe, nota) }
    }

    fun guardaMiAvatar(monigote: Monigote) {
        viewModelScope.launch { repo.guardaMiAvatar(monigote.serializa()) }
    }

    fun guardaAvatarDe(colegaId: String, monigote: Monigote) {
        viewModelScope.launch { repo.guardaAvatarDe(colegaId, monigote.serializa()) }
    }

    class Fabrica(
        private val repo: Repositorio,
        private val nube: RepositorioNube? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PulgaresViewModel(repo, nube) as T
    }
}
