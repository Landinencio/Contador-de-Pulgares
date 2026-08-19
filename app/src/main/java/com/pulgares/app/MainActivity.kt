package com.pulgares.app

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulgares.app.data.EstadoGrupo
import com.pulgares.app.data.Repositorio
import com.pulgares.app.data.RepositorioNube
import com.pulgares.app.data.local.BaseDatos
import com.pulgares.app.data.red.IdentidadMovil
import com.pulgares.app.domain.model.Dinero
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento
import com.pulgares.app.notificaciones.CobradorWorker
import com.pulgares.app.notificaciones.Zumbador
import com.pulgares.app.ui.components.ZumbidoOverlay
import androidx.compose.ui.graphics.graphicsLayer
import com.pulgares.app.ui.PulgaresViewModel
import com.pulgares.app.ui.components.LluviaDeConfeti
import com.pulgares.app.ui.components.recuerdaCelebracion
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.ui.screens.BloqueCompartir
import com.pulgares.app.ui.screens.BloqueSolicitudes
import com.pulgares.app.ui.screens.DetalleGrupoScreen
import com.pulgares.app.ui.screens.DialogoUnirse
import com.pulgares.app.ui.screens.EditarGrupoScreen
import com.pulgares.app.ui.screens.EditorAvatarScreen
import com.pulgares.app.ui.screens.avatarDe
import com.pulgares.app.ui.screens.NuevoGastoScreen
import com.pulgares.app.ui.screens.NuevoGrupoScreen
import com.pulgares.app.ui.screens.PerfilScreen
import com.pulgares.app.ui.screens.PortadaScreen
import com.pulgares.app.ui.theme.TemaPulgares
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bd = BaseDatos.obten(applicationContext)
        val repo = Repositorio(bd)
        val identidad = IdentidadMovil(applicationContext)
        // La sincronización es opcional: sin token en el build, ClienteNube se
        // declara no disponible y la app se queda como estaba, 100% local.
        val nube = RepositorioNube(bd, repo, identidad)
        setContent {
            TemaPulgares {
                AppPulgares(repo, nube, identidad)
            }
        }
    }
}

/** Las pantallas de la app. Son pocas; no hace falta traerse una libreria. */
private sealed interface Pantalla {
    data object Portada : Pantalla
    data object NuevoGrupo : Pantalla
    data class Grupo(val grupoId: String) : Pantalla
    data class Gasto(val grupoId: String, val gastoId: String?) : Pantalla
    data class AjustesGrupo(val grupoId: String) : Pantalla
    data object MiAvatar : Pantalla
    data class AvatarDeColega(val grupoId: String, val colegaId: String) : Pantalla

    /** El grupo al que pertenece la pantalla, si va de un grupo concreto. */
    val grupoAsociado: String?
        get() = when (this) {
            is Grupo -> grupoId
            is Gasto -> grupoId
            is AjustesGrupo -> grupoId
            is AvatarDeColega -> grupoId
            else -> null
        }

    /** A donde lleva el boton atras del movil. */
    fun atras(): Pantalla = when (this) {
        Portada -> Portada
        NuevoGrupo, MiAvatar -> Portada
        is Grupo -> Portada
        is Gasto -> Grupo(grupoId)
        is AjustesGrupo -> Grupo(grupoId)
        is AvatarDeColega -> AjustesGrupo(grupoId)
    }

    /**
     * Se guarda como texto para que la pantalla abierta sobreviva a girar el
     * movil y a que Android mate la app por falta de memoria.
     */
    fun serializa(): String = when (this) {
        Portada -> "portada"
        NuevoGrupo -> "nuevoGrupo"
        MiAvatar -> "miAvatar"
        is Grupo -> "grupo|$grupoId"
        is Gasto -> "gasto|$grupoId|${gastoId.orEmpty()}"
        is AjustesGrupo -> "ajustes|$grupoId"
        is AvatarDeColega -> "avatarColega|$grupoId|$colegaId"
    }

    companion object {
        fun desde(texto: String): Pantalla {
            val trozos = texto.split("|")
            return when (trozos.firstOrNull()) {
                "nuevoGrupo" -> NuevoGrupo
                "miAvatar" -> MiAvatar
                "grupo" -> trozos.getOrNull(1)?.let { Grupo(it) } ?: Portada
                "gasto" -> trozos.getOrNull(1)?.let {
                    Gasto(it, trozos.getOrNull(2)?.ifBlank { null })
                } ?: Portada
                "ajustes" -> trozos.getOrNull(1)?.let { AjustesGrupo(it) } ?: Portada
                "avatarColega" -> {
                    val grupo = trozos.getOrNull(1)
                    val colega = trozos.getOrNull(2)
                    if (grupo != null && colega != null) AvatarDeColega(grupo, colega) else Portada
                }
                else -> Portada
            }
        }
    }
}

@Composable
fun AppPulgares(
    repo: Repositorio,
    nube: RepositorioNube? = null,
    identidad: IdentidadMovil? = null
) {
    val vm: PulgaresViewModel = viewModel(factory = PulgaresViewModel.Fabrica(repo, identidad, nube))
    val grupos by vm.grupos.collectAsStateWithLifecycle()
    val estadoGrupo by vm.estadoGrupo.collectAsStateWithLifecycle()
    val miAvatar by vm.miAvatar.collectAsStateWithLifecycle()

    var pantalla by rememberSaveable(
        stateSaver = Saver(
            save = { it.serializa() },
            restore = { Pantalla.desde(it) }
        )
    ) { mutableStateOf<Pantalla>(Pantalla.Portada) }

    val sincronizando by vm.sincronizando.collectAsStateWithLifecycle()
    val perfil by vm.perfil.collectAsStateWithLifecycle()
    val perfilCargado by vm.perfilCargado.collectAsStateWithLifecycle()
    val pendientes by vm.pendientes.collectAsStateWithLifecycle()
    val solicitudesPorGrupo by vm.solicitudes.collectAsStateWithLifecycle()
    var uniendose by remember { mutableStateOf(false) }
    var pedidaA by remember { mutableStateOf<String?>(null) }
    var recuperacion by remember { mutableStateOf<String?>(null) }

    val portapapeles = LocalClipboardManager.current
    val contexto = LocalContext.current
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()

    // ---- el contrato del Cobrador del Frac ----
    var cobradorContratado by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(identidad) {
        val contratado = identidad?.cobradorContratado() ?: false
        cobradorContratado = contratado
        // El cobrador viene de fábrica: si está contratado, la ronda diaria se
        // asegura en cada arranque (nadie pulsó "contratar" para programarla).
        if (contratado) {
            CobradorWorker.asegura(contexto)
        }
    }
    // En Android 13+ las notificaciones piden permiso en tiempo de ejecución;
    // se pide justo al contratar, que es cuando tiene sentido para el usuario.
    val pidePermisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            alcance.launch {
                avisos.showSnackbar("Sin el permiso de notificaciones, el cobrador es mudo.")
            }
        }
    }

    // El cobrador viene de fábrica, así que el permiso de notificaciones se pide
    // una única vez al arrancar (insistir en cada arranque quema al usuario).
    LaunchedEffect(cobradorContratado) {
        if (cobradorContratado == true && identidad != null &&
            !identidad.permisoNotisYaPedido() &&
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            identidad.marcaPermisoNotisPedido()
            pidePermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun contrataCobrador() {
        if (identidad == null) return
        cobradorContratado = true
        alcance.launch { identidad.contrataCobrador(true) }
        CobradorWorker.contrata(contexto)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pidePermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        alcance.launch {
            avisos.showSnackbar("Contratado. Cobra en frases, no en comisión.")
        }
    }

    fun despideCobrador() {
        if (identidad == null) return
        cobradorContratado = false
        alcance.launch { identidad.contrataCobrador(false) }
        CobradorWorker.despide(contexto)
        alcance.launch { avisos.showSnackbar("El cobrador cuelga el frac. De momento.") }
    }

    fun avisa(texto: String) {
        alcance.launch { avisos.showSnackbar(texto) }
    }

    // El atrás del móvil recorre la jerarquía en vez de cerrar la app. En la
    // portada se deja pasar, que ahí sí toca salir.
    BackHandler(enabled = pantalla != Pantalla.Portada) {
        pantalla = pantalla.atras()
    }

    // Si Android mató la app y se restaura en la pantalla de un grupo, el
    // ViewModel viene vacío: hay que volver a pedir ese grupo o se queda
    // cargando para siempre.
    LaunchedEffect(pantalla.grupoAsociado) {
        pantalla.grupoAsociado?.let { vm.abreGrupo(it) }
    }

    // Al arrancar la app se sincronizan todos los grupos compartidos, para que
    // la portada (los saldos) ya refleje lo que apuntaron los demás.
    LaunchedEffect(Unit) {
        vm.sincronizaTodosLosCompartidos()
    }

    // Con un grupo compartido en pantalla se sincroniza en silencio: una vez al
    // abrirlo y luego un pulso cada 30 segundos. Así, dos colegas con la app
    // abierta a la vez se ven los gastos sin tocar nada (el pulso muere solo al
    // salir del grupo, porque el LaunchedEffect se cancela al cambiar la clave).
    val grupoCompartidoAbierto = estadoGrupo?.grupo?.takeIf {
        it.compartido && it.id == pantalla.grupoAsociado
    }?.id
    LaunchedEffect(grupoCompartidoAbierto) {
        val grupoId = grupoCompartidoAbierto ?: return@LaunchedEffect
        while (true) {
            vm.sincronizaEnSilencio(grupoId) { novedades ->
                if (novedades.solicitudes.isNotEmpty()) {
                    avisa(
                        if (novedades.solicitudes.size == 1) {
                            "🛎️ ${novedades.solicitudes.first().nombre} quiere entrar: apruébalo en ajustes."
                        } else {
                            "🛎️ ${novedades.solicitudes.size} personas quieren entrar: apruébalas en ajustes."
                        }
                    )
                } else {
                    avisa(resumenSync(novedades))
                }
            }
            kotlinx.coroutines.delay(30_000)
        }
    }

    // ---- el zumbido: vibración y pantalla temblando, como en 2006 ----
    val zumbido by vm.zumbidoRecibido.collectAsStateWithLifecycle()
    val sacudida = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(zumbido?.id) {
        val evento = zumbido ?: return@LaunchedEffect
        Zumbador.zumba(contexto)
        // Ocho bandazos y de vuelta al centro: el temblor clásico.
        repeat(8) { i ->
            sacudida.animateTo(
                if (i % 2 == 0) 16f else -16f,
                androidx.compose.animation.core.tween(durationMillis = 45)
            )
        }
        sacudida.animateTo(0f, androidx.compose.animation.core.tween(durationMillis = 70))
    }

    // Un grupo recién saldado merece confeti. Sin gastos no cuenta: estar a cero
    // porque no has gastado nada no es ningún logro.
    val grupoAbierto = estadoGrupo
    val (celebrar, finCelebracion) = recuerdaCelebracion(
        enPaz = grupoAbierto != null && grupoAbierto.enPaz && grupoAbierto.gastos.isNotEmpty(),
        de = grupoAbierto?.grupo?.id
    )

    // ---- primer arranque: sin perfil no se pasa de aquí ----
    // El perfil es lo que viaja al pedir entrar en un grupo y lo que te
    // identifica al crear uno, así que se pide una vez y de frente.
    if (!perfilCargado) {
        Cargando()
        return
    }
    if (perfil == null) {
        // A quien ya usaba la app se le sugiere el nombre y monigote de su
        // "yo" más reciente, para no hacerle escribirlo otra vez.
        var sugerido by remember { mutableStateOf<IdentidadMovil.Perfil?>(null) }
        var sugerenciaLista by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            sugerido = vm.perfilSugerido()
            sugerenciaLista = true
        }
        if (!sugerenciaLista) {
            Cargando()
            return
        }
        val avatarInicial = remember(sugerido) {
            Monigote.parse(sugerido?.avatar) ?: Monigote.aleatorio()
        }
        PerfilScreen(
            nombreInicial = sugerido?.nombre.orEmpty(),
            avatarInicial = avatarInicial,
            esPrimeraVez = true,
            onGuardar = { nombre, monigote -> vm.guardaPerfil(nombre, monigote) }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = MaterialTheme.colorScheme.background
    ) { margenes ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(margenes)
                .graphicsLayer { translationX = sacudida.value }
        ) {
            when (val actual = pantalla) {
                Pantalla.Portada -> PortadaScreen(
                    grupos = grupos,
                    miAvatar = miAvatar,
                    onAbrirGrupo = { id ->
                        vm.abreGrupo(id)
                        pantalla = Pantalla.Grupo(id)
                    },
                    onNuevoGrupo = { pantalla = Pantalla.NuevoGrupo },
                    onEditarAvatar = { pantalla = Pantalla.MiAvatar },
                    onUnirse = if (vm.nubeDisponible) {
                        { uniendose = true }
                    } else {
                        null
                    },
                    pendientes = pendientes,
                    onComprobarPendientes = {
                        vm.compruebaPendientes(
                            onHecho = { resultados ->
                                resultados.forEach { (pendiente, resultado) ->
                                    when (resultado) {
                                        is RepositorioNube.Solicitud.Dentro -> {
                                            pantalla = Pantalla.Grupo(resultado.grupoId)
                                            avisa("¡Dentro de «${pendiente.nombreGrupo}»! Que empiece el drama.")
                                        }

                                        is RepositorioNube.Solicitud.Rechazada ->
                                            avisa("El dueño de «${pendiente.nombreGrupo}» ha dicho que no.")

                                        is RepositorioNube.Solicitud.Pendiente ->
                                            avisa("«${pendiente.nombreGrupo}» sigue sin respuesta. Dale un toque al dueño.")
                                    }
                                }
                            },
                            onError = { avisa(it) }
                        )
                    },
                    cobradorContratado = if (identidad == null) null else cobradorContratado,
                    onContratarCobrador = ::contrataCobrador,
                    onDespedirCobrador = ::despideCobrador
                )

                Pantalla.NuevoGrupo -> NuevoGrupoScreen(
                    miNombre = perfil?.nombre ?: "Yo",
                    miAvatar = miAvatar,
                    onCrear = { nombre, emoji ->
                        vm.creaGrupo(nombre, emoji) { id ->
                            pantalla = Pantalla.Grupo(id)
                        }
                    },
                    onVolver = { pantalla = Pantalla.Portada }
                )

                is Pantalla.Grupo -> {
                    val estado = estadoGrupo
                    if (estado == null || estado.grupo.id != actual.grupoId) {
                        Cargando()
                    } else {
                        DetalleGrupoScreen(
                            estado = estado,
                            onVolver = { pantalla = Pantalla.Portada },
                            onNuevoGasto = { pantalla = Pantalla.Gasto(actual.grupoId, null) },
                            onEditarGasto = { gasto ->
                                pantalla = Pantalla.Gasto(actual.grupoId, gasto.id)
                            },
                            onVotar = { gastoId, arriba ->
                                val miId = estado.grupo.yo?.id
                                if (miId != null) vm.votaGasto(gastoId, miId, arriba)
                            },
                            onPagar = { transferencia ->
                                vm.registraPago(
                                    grupoId = actual.grupoId,
                                    deQuienId = transferencia.deQuienId,
                                    aQuienId = transferencia.aQuienId,
                                    importe = transferencia.importeCentimos
                                )
                                val soyYo = transferencia.deQuienId == estado.grupo.yo?.id
                                avisa(
                                    if (soyYo) {
                                        Frases.para(
                                            Momento.PAGASTE,
                                            quien = estado.grupo.nombreDe(transferencia.deQuienId),
                                            centimos = transferencia.importeCentimos
                                        )
                                    } else {
                                        Frases.para(
                                            Momento.TE_HAN_PAGADO,
                                            quien = estado.grupo.nombreDe(transferencia.deQuienId),
                                            cuanto = Dinero.formatea(transferencia.importeCentimos)
                                        )
                                    }
                                )
                            },
                            onDarToque = { colega, deuda ->
                                if (estado.grupo.compartido && vm.nubeDisponible) {
                                    vm.zumba(
                                        actual.grupoId,
                                        colega.id,
                                        onHecho = {
                                            avisa(
                                                Frases.para(
                                                    Momento.TOQUE,
                                                    quien = colega.nombre,
                                                    centimos = deuda
                                                )
                                            )
                                        },
                                        onError = { avisa(it) }
                                    )
                                } else {
                                    avisa(
                                        Frases.para(
                                            Momento.DEBES,
                                            quien = colega.nombre,
                                            centimos = deuda
                                        )
                                    )
                                }
                            },
                            onAbrirAjustes = { pantalla = Pantalla.AjustesGrupo(actual.grupoId) },
                            onZumbar = if (estado.grupo.compartido && vm.nubeDisponible) {
                                { colegaId ->
                                    vm.zumba(
                                        actual.grupoId,
                                        colegaId,
                                        onHecho = { veces ->
                                            val nivel = Frases.nivelZumbido(veces)
                                            val rango = Frases.rangoZumbido(veces)
                                            avisa(
                                                if (veces > 1) {
                                                    "Zumbido nº$veces enviado. " +
                                                        "Nivel $nivel: $rango."
                                                } else {
                                                    "Zumbido enviado. Nivel $nivel: $rango."
                                                }
                                            )
                                        },
                                        onError = { avisa(it) }
                                    )
                                }
                            } else {
                                null
                            },
                            onBorrarPago = { pago ->
                                vm.borraPago(pago.id)
                                avisa("Bizum deshecho. Las cuentas vuelven a como estaban.")
                            }
                        )
                    }
                }

                is Pantalla.AjustesGrupo -> {
                    val estado = estadoGrupo
                    if (estado == null || estado.grupo.id != actual.grupoId) {
                        Cargando()
                    } else {
                        EditarGrupoScreen(
                            estado = estado,
                            onGuardarNombre = { nombre, emoji ->
                                vm.renombraGrupo(estado.grupo, nombre, emoji)
                                avisa("Grupo actualizado.")
                            },
                            onAnadirColega = { nombre ->
                                vm.anadeColega(actual.grupoId, nombre, estado.grupo.colegas.size)
                                avisa("$nombre se une al grupo. Que se prepare.")
                            },
                            onQuitarColega = { colega ->
                                vm.quitaColega(actual.grupoId, estado.grupo.colegas, colega)
                                avisa("${colega.nombre} sale del grupo. Sus gastos se quedan.")
                            },
                            onRenombrarColega = { colega, nombre ->
                                vm.renombraColega(actual.grupoId, estado.grupo.colegas, colega, nombre)
                            },
                            onReadmitirColega = { colega ->
                                vm.readmiteColega(actual.grupoId, estado.grupo.colegas, colega)
                                avisa("${colega.nombre} vuelve al grupo.")
                            },
                            onEditarAvatarDe = { colega ->
                                pantalla = Pantalla.AvatarDeColega(actual.grupoId, colega.id)
                            },
                            onBorrarGrupo = {
                                vm.borraGrupo(actual.grupoId)
                                pantalla = Pantalla.Portada
                                avisa("Grupo borrado. Aquí no ha pasado nada.")
                            },
                            onVolver = { pantalla = Pantalla.Grupo(actual.grupoId) },
                            bloqueCompartir = {
                                // El código de recuperación se pide al entrar aquí.
                                LaunchedEffect(estado.grupo.remotoId) {
                                    vm.codigoDeRecuperacion(actual.grupoId) { recuperacion = it }
                                }
                                val resultado = solicitudesPorGrupo[actual.grupoId]
                                if (resultado != null && resultado.solicitudes.isNotEmpty()) {
                                    BloqueSolicitudes(
                                        solicitudes = resultado.solicitudes,
                                        colegasLibres = resultado.colegasLibres,
                                        sincronizando = sincronizando,
                                        onAprobar = { uid, colegaId ->
                                            vm.apruebaSolicitud(
                                                actual.grupoId, uid, colegaId,
                                                onHecho = { avisa("Dentro. Uno más a repartir.") },
                                                onError = { avisa(it) }
                                            )
                                        },
                                        onRechazar = { uid ->
                                            vm.rechazaSolicitud(
                                                actual.grupoId, uid,
                                                onHecho = { avisa("Rechazado. Sin rencores (o sí).") },
                                                onError = { avisa(it) }
                                            )
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                BloqueCompartir(
                                    grupo = estado.grupo,
                                    disponible = vm.nubeDisponible,
                                    sincronizando = sincronizando,
                                    codigoRecuperacion = recuperacion,
                                    onCompartir = {
                                        vm.comparte(
                                            actual.grupoId,
                                            onHecho = { codigo ->
                                                avisa("Compartido. El código es $codigo")
                                            },
                                            onError = { avisa(it) }
                                        )
                                    },
                                    onSincronizar = {
                                        vm.sincroniza(
                                            actual.grupoId,
                                            onHecho = { resultado ->
                                                avisa(resumenSync(resultado))
                                            },
                                            onError = { avisa(it) }
                                        )
                                    },
                                    onRotarCodigo = {
                                        vm.rotaCodigo(
                                            actual.grupoId,
                                            onHecho = { avisa("Código nuevo: $it") },
                                            onError = { avisa(it) }
                                        )
                                    },
                                    onDejarDeCompartir = {
                                        vm.dejaDeCompartir(
                                            actual.grupoId,
                                            onHecho = { avisa("El grupo se queda en este móvil.") },
                                            onError = { avisa(it) }
                                        )
                                    },
                                    onCopiarCodigo = { codigo ->
                                        portapapeles.setText(AnnotatedString(codigo))
                                        avisa("Código copiado: pégalo en el grupo de WhatsApp.")
                                    }
                                )
                            }
                        )
                    }
                }

                is Pantalla.AvatarDeColega -> {
                    val estado = estadoGrupo
                    val colega = estado?.grupo?.colega(actual.colegaId)
                    if (estado == null || colega == null) {
                        Cargando()
                    } else {
                        EditorAvatarScreen(
                            inicial = avatarDe(colega),
                            titulo = "El monigote de ${colega.nombre}",
                            onGuardar = { monigote ->
                                vm.guardaAvatarDe(colega.id, monigote)
                                pantalla = Pantalla.AjustesGrupo(actual.grupoId)
                                avisa("Así se queda ${colega.nombre}.")
                            },
                            onVolver = { pantalla = Pantalla.AjustesGrupo(actual.grupoId) }
                        )
                    }
                }

                is Pantalla.Gasto -> {
                    val estado = estadoGrupo
                    // Se comprueba tambien el id: al cambiar de grupo el flujo
                    // tarda un instante en traer el nuevo, y sin esto la pantalla
                    // pintaria los colegas del grupo anterior.
                    if (estado == null || estado.grupo.id != actual.grupoId) {
                        Cargando()
                    } else {
                        val existente: Gasto? = actual.gastoId?.let { id ->
                            estado.gastos.firstOrNull { it.id == id }
                        }
                        NuevoGastoScreen(
                            // Los que ya no están en el grupo solo aparecen si el
                            // gasto que se edita los incluía: así se mantiene su
                            // parte y no se recalculan cuentas ya cerradas.
                            colegas = colegasDelGasto(estado, existente),
                            gastoExistente = existente,
                            onGuardar = { concepto, importe, pagadorId, categoria, reparto, nota ->
                                vm.apuntaGasto(
                                    grupoId = actual.grupoId,
                                    concepto = concepto,
                                    importeCentimos = importe,
                                    pagadorId = pagadorId,
                                    categoria = categoria,
                                    reparto = reparto,
                                    nota = nota,
                                    original = existente
                                )
                                pantalla = Pantalla.Grupo(actual.grupoId)
                                avisa(
                                    Frases.para(
                                        Momento.GASTO_NUEVO,
                                        quien = estado.grupo.nombreDe(pagadorId),
                                        centimos = importe,
                                        que = concepto
                                    )
                                )
                            },
                            onBorrar = existente?.let { gasto ->
                                {
                                    vm.borraGasto(gasto.id)
                                    pantalla = Pantalla.Grupo(actual.grupoId)
                                }
                            },
                            onVolver = { pantalla = Pantalla.Grupo(actual.grupoId) }
                        )
                    }
                }

                Pantalla.MiAvatar -> PerfilScreen(
                    nombreInicial = perfil?.nombre.orEmpty(),
                    avatarInicial = miAvatar,
                    esPrimeraVez = false,
                    onGuardar = { nombre, monigote ->
                        vm.guardaPerfil(nombre, monigote) {
                            pantalla = Pantalla.Portada
                            avisa("Perfil guardado. Menudo careto.")
                        }
                    },
                    onVolver = { pantalla = Pantalla.Portada }
                )
            }

            if (uniendose) {
                DialogoUnirse(
                    miNombre = perfil?.nombre ?: "Yo",
                    miAvatar = miAvatar,
                    sincronizando = sincronizando,
                    pedidaA = pedidaA,
                    onPedir = { codigo ->
                        vm.solicita(
                            codigo,
                            onHecho = { resultado ->
                                when (resultado) {
                                    is RepositorioNube.Solicitud.Dentro -> {
                                        uniendose = false
                                        pedidaA = null
                                        pantalla = Pantalla.Grupo(resultado.grupoId)
                                        avisa("Dentro. Que empiece el drama.")
                                    }

                                    is RepositorioNube.Solicitud.Pendiente -> {
                                        pedidaA = resultado.nombreGrupo
                                    }

                                    is RepositorioNube.Solicitud.Rechazada -> {
                                        uniendose = false
                                        pedidaA = null
                                        avisa("El dueño de «${resultado.nombreGrupo}» ha dicho que no. Habla con él.")
                                    }
                                }
                            },
                            onError = { avisa(it) }
                        )
                    },
                    onCerrar = {
                        uniendose = false
                        pedidaA = null
                    }
                )
            }

            // Va al final del Box para caer por encima de cualquier pantalla.
            LluviaDeConfeti(dispara = celebrar, onFin = finCelebracion)

            zumbido?.let { evento ->
                ZumbidoOverlay(zumbido = evento, onVisto = { vm.zumbidoVisto() })
            }
        }
    }
}

/**
 * Quién puede salir en la pantalla de un gasto: los que están en el grupo, más
 * los que ya se fueron pero participaban en ese gasto concreto (incluido quien
 * lo pagó). Sin esto, editar la categoría de un gasto viejo lo re-repartía entre
 * los que quedan y cambiaba deudas que el grupo ya había cerrado.
 */
private fun colegasDelGasto(estado: EstadoGrupo, gasto: Gasto?): List<com.pulgares.app.domain.model.Colega> {
    val activos = estado.grupo.activos
    if (gasto == null) return activos

    val implicados = gasto.reparto.implicados.toSet() + gasto.pagadorId
    val salidosQueImportan = estado.grupo.salidos.filter { it.id in implicados }
    return activos + salidosQueImportan
}

/** Qué contarle al usuario después de sincronizar. */
private fun resumenSync(resultado: RepositorioNube.Resultado): String = when {
    resultado.gastosNuevos == 0 && resultado.pagosNuevos == 0 ->
        "Todo al día: no había nada nuevo."
    resultado.gastosNuevos > 0 && resultado.pagosNuevos > 0 ->
        "Llegan ${resultado.gastosNuevos} gastos y ${resultado.pagosNuevos} bizums nuevos."
    resultado.gastosNuevos == 1 -> "Llega un gasto nuevo."
    resultado.gastosNuevos > 1 -> "Llegan ${resultado.gastosNuevos} gastos nuevos."
    resultado.pagosNuevos == 1 -> "Llega un bizum nuevo."
    else -> "Llegan ${resultado.pagosNuevos} bizums nuevos."
}

@Composable
private fun Cargando() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Contando pulgares…",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(24.dp)
        )
    }
}
