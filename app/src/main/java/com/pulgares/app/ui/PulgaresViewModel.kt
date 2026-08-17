package com.pulgares.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.data.EstadoGrupo
import com.pulgares.app.data.Repositorio
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
class PulgaresViewModel(private val repo: Repositorio) : ViewModel() {

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
     * Saca a un colega de la lista. Sus gastos NO se tocan: reescribir gastos ya
     * repartidos cambiaria cuentas que el grupo ya dio por buenas.
     */
    fun quitaColega(grupoId: String, colegas: List<Colega>, fuera: Colega) {
        viewModelScope.launch {
            repo.guardaColegas(grupoId, colegas.filterNot { it.id == fuera.id })
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

    /** Apunta un gasto nuevo. Devuelve el gasto guardado por si hay que celebrarlo. */
    fun apuntaGasto(
        grupoId: String,
        concepto: String,
        importeCentimos: Long,
        pagadorId: String,
        categoria: Categoria,
        reparto: Reparto,
        nota: String?,
        gastoExistenteId: String? = null
    ) {
        viewModelScope.launch {
            repo.guardaGasto(
                Gasto(
                    id = gastoExistenteId ?: Repositorio.nuevoId(),
                    grupoId = grupoId,
                    concepto = concepto.ifBlank { "Sin nombre" },
                    importeCentimos = importeCentimos,
                    pagadorId = pagadorId,
                    fechaMillis = System.currentTimeMillis(),
                    categoria = categoria,
                    reparto = reparto,
                    nota = nota?.ifBlank { null }
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

    class Fabrica(private val repo: Repositorio) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PulgaresViewModel(repo) as T
    }
}
