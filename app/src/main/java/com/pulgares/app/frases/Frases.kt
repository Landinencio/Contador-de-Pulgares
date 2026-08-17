package com.pulgares.app.frases

import kotlin.random.Random

/**
 * El alma de la app. Cada momento tiene su coña: cuando debes, cuando pagas,
 * cuando te deben y cuando alguien lleva tres meses haciendose el sueco.
 *
 * Convenciones de las plantillas:
 *   {quien}  -> nombre del colega
 *   {cuanto} -> importe ya formateado ("12,50 €")
 *   {que}    -> concepto del gasto
 *   {dias}   -> dias que lleva la deuda sin saldar
 * Ninguna frase usa un hueco que no le pasen: ver FrasesTest.
 */
enum class Momento {
    /** Recordatorio al que debe. El clasico "paga, perro". */
    DEBES,

    /** Le acabas de pasar dinero a alguien. */
    PAGASTE,

    /** Alguien te ha pagado a ti. */
    TE_HAN_PAGADO,

    /** Resumen de que te deben dinero. */
    TE_DEBEN,

    /** Se acaba de anadir un gasto al grupo. */
    GASTO_NUEVO,

    /** El grupo entero esta a cero. Fiesta. */
    EN_PAZ,

    /** Deuda con mas telarana que el bolsillo del moroso. */
    MOROSO_LEYENDA,

    /** Toque manual: el boton de dar la lata. */
    TOQUE,

    /** Aun no hay gastos en el grupo. */
    SIN_GASTOS,

    /** Aun no hay grupos creados. */
    SIN_GRUPOS,

    /** Cabecera cuando el usuario esta en numeros rojos. */
    CABECERA_DEBO,

    /** Cabecera cuando al usuario le deben. */
    CABECERA_ME_DEBEN
}

object Frases {

    private val debes = listOf(
        "¡Paga, perro! 🐕",
        "Suelta la mosca, {quien}.",
        "{quien}, debes {cuanto}. Y lo sabes.",
        "Afloja esos {cuanto} antes de que se enfríen.",
        "El que paga descansa. Tú debes de estar agotadísimo.",
        "{cuanto}. Ni un céntimo más, ni uno menos. Sobre todo menos.",
        "Tu cartera ha echado telarañas. {cuanto}.",
        "Recordatorio cariñoso: {cuanto}. El siguiente no será cariñoso.",
        "Vuelva usted mañana, decías. Ya es mañana: {cuanto}.",
        "{quien}, esto no es una ONG. {cuanto}.",
        "Se te cayó la cartera. Hace semanas. {cuanto}.",
        "El Bizum no muerde, {quien}. {cuanto}.",
        "Menos lobos, más {cuanto}.",
        "Deuda pendiente: {cuanto}. Excusas pendientes: infinitas.",
        "Aquí un poquito de por favor y {cuanto}.",
        "{quien}, el grupo te mira fijamente. {cuanto}.",
        "Ojo por ojo, euro por euro: {cuanto}.",
        "No es por presionar, pero {cuanto}. Sí es por presionar.",
        "Tic, tac. {cuanto}, {quien}.",
        "Tus {cuanto} y mi paciencia se están agotando a la vez."
    )

    private val pagaste = listOf(
        "Un Lannister siempre paga sus deudas. 🦁",
        "Pagado. Puedes volver a mirar al grupo a los ojos.",
        "{cuanto} fuera. Qué señor.",
        "Deuda saldada. Alguien avise a los juglares.",
        "Has pagado. Sí, tú. Que quede constancia.",
        "Y el pueblo dijo: ¡por fin!",
        "{cuanto} volando. Honor recuperado.",
        "Pagado y sin rechistar. Nivel: leyenda.",
        "Ojalá todos como tú, {quien}.",
        "El que paga descansa. Descansa, campeón.",
        "Cuentas claras, amistad conservada.",
        "Bizum enviado, karma equilibrado.",
        "{cuanto} menos en tu cuenta, un amigo más en tu vida.",
        "Se ha hecho justicia. Y ha costado {cuanto}.",
        "Toma ya. Pagador certificado. 🏅",
        "Menos de un minuto en pagar. Toma nota, el resto.",
        "Has pagado antes de que te lo pidieran. ¿Estás bien?",
        "Deuda: cero. Dignidad: intacta.",
        "Aquí un valiente que suelta {cuanto} sin llorar.",
        "Y así, sin más, {quien} dejó de ser el malo de la película."
    )

    private val teHanPagado = listOf(
        "{quien} te ha pasado {cuanto}. Milagro documentado. ✨",
        "¡{cuanto} de {quien}! Ya puedes dejar de mirarle mal.",
        "{quien} ha pagado. Apúntalo en el calendario.",
        "Entran {cuanto}. La cartera respira.",
        "{quien} soltó {cuanto}. Sin llorar apenas.",
        "Cobrado: {cuanto}. Un placer hacer negocios contigo, {quien}.",
        "{quien} ha aflojado la mosca: {cuanto}.",
        "Ha caído la transferencia. {cuanto} de {quien}.",
        "{cuanto} recuperados. Justicia poética.",
        "{quien} te debe una menos. Y {cuanto} menos.",
        "Dinero de vuelta a casa: {cuanto}.",
        "Y {quien} dijo: toma tus {cuanto} y déjame en paz.",
        "{quien} ha pagado sin que se lo recuerdes. Un fenómeno.",
        "Suenan las trompetas: {quien} ha soltado {cuanto}.",
        "{cuanto} en la saca. {quien} vuelve a ser de fiar.",
        "Pago recibido. Puedes borrar el mensaje pasivo-agresivo que tenías a medias.",
        "{quien} ha cumplido. Que conste en el acta del grupo.",
        "{cuanto} cobrados. La fe en la humanidad, restaurada."
    )

    private val teDeben = listOf(
        "Te deben {cuanto}. Eres el banco del grupo.",
        "{cuanto} ahí fuera, esperando volver a casa.",
        "Vas de acreedor por la vida: {cuanto}.",
        "Te deben {cuanto}. Cobra sin piedad.",
        "Pagafantas nivel {cuanto}.",
        "Sois amigos, pero {cuanto} son {cuanto}.",
        "El grupo te debe {cuanto} y su eterna gratitud. Cobra lo primero.",
        "Has puesto {cuanto} de más. San {quien}, patrón de las rondas.",
        "{cuanto} pendientes de cobro. Da el toque, no seas tímido.",
        "Tienes {cuanto} en la calle. Ve a por ellos.",
        "Mecenas del grupo por {cuanto}.",
        "Cajero automático humano: te deben {cuanto}.",
        "Eres el Lannister de este grupo: te deben {cuanto} y aún sonríes.",
        "{cuanto} prestados sin intereses. Muy generoso. Demasiado.",
        "Has financiado la fiesta con {cuanto}. Es hora de pasar por caja.",
        "{cuanto} pendientes. Recuerda: prestar es de amigos, cobrar también.",
        "Tu saldo dice {cuanto} a favor. Tu cuenta del banco no lo sabe todavía.",
        "El grupo funciona con tu dinero: {cuanto}."
    )

    private val gastoNuevo = listOf(
        "Anotado: {que}. Que empiece el drama.",
        "{que} apuntado. {cuanto} al saco.",
        "Nuevo gasto: {que}. Alguien va a llorar.",
        "{cuanto} en {que}. Sin arrepentimientos.",
        "Apuntado. El contador de pulgares no perdona.",
        "{que}: {cuanto}. Que conste en acta.",
        "Registrado. La deuda es eterna, como el amor.",
        "{que} añadido. Preparad los bizums.",
        "Y {quien} volvió a poner la tarjeta. {cuanto}.",
        "{cuanto} de {que}. Barato para lo que nos reímos.",
        "Gasto anotado. Nadie escapa de la hoja de cálculo.",
        "{que}. Que cada palo aguante su vela: {cuanto}.",
        "{cuanto} más a la cuenta común. La bola de nieve crece.",
        "Otro gasto de {que}. Esto se nos está yendo de las manos.",
        "{que} apuntado. El grupo tiene memoria de elefante.",
        "Añadido {que} por {cuanto}. Nadie dirá que no llevabas las cuentas.",
        "{cuanto} en {que}. Que nadie diga luego que no se enteró.",
        "Nuevo gasto en el bote: {que}."
    )

    private val enPaz = listOf(
        "¡Todo en paz! Nadie debe nada a nadie. 🕊️",
        "Cuentas a cero. Ya podéis volver a ser amigos.",
        "Grupo saldado. Esto hay que celebrarlo con otro gasto.",
        "Cero deudas. Cero excusas. Cero drama.",
        "Todos limpios. Da miedo.",
        "Ni un céntimo pendiente. Qué grupo tan raro.",
        "Cuentas claras y chocolate espeso. ✅",
        "Nadie debe nada. Momento histórico.",
        "Paz financiera absoluta. Que dure.",
        "Se cierra el expediente. Todos absueltos.",
        "Balance cero: el grupo ha alcanzado el nirvana.",
        "Todo cuadrado. Ahora id a gastar otra vez.",
        "Todos han pagado. Que alguien lo grabe para la posteridad.",
        "Cero pendiente. Hasta los Lannister estarían orgullosos.",
        "Deudas: ninguna. Amistades: intactas. Objetivo cumplido.",
        "El contador está a cero y nadie ha discutido. Milagro doble.",
        "Cuentas saldadas. Ya podéis hablar de otra cosa en el grupo.",
        "Nadie debe nada. Disfrutad de esta paz efímera."
    )

    private val morosoLeyenda = listOf(
        "{quien} lleva {dias} días debiendo {cuanto}. Ya es patrimonio.",
        "{dias} días. {quien} ha convertido la deuda en un estilo de vida.",
        "Aquí {quien}, debiendo {cuanto} desde hace {dias} días. Un clásico.",
        "{quien} debe {cuanto}. Ha pasado tanto tiempo que ya es tradición.",
        "Récord del grupo: {quien}, {dias} días sin soltar {cuanto}.",
        "{dias} días. A este ritmo, la deuda cumple la mayoría de edad.",
        "{quien} + {cuanto} + {dias} días = leyenda viva del sablazo.",
        "Se busca: {quien}. Recompensa: {cuanto}.",
        "{quien} lleva {dias} días entrenando para las olimpiadas del moroso.",
        "La deuda de {quien} ya tiene {dias} días. Habrá que ponerle nombre.",
        "{cuanto} desde hace {dias} días. {quien}, esto ya es arte.",
        "Nivel dios: {quien}, {dias} días sin pagar {cuanto} y durmiendo tan tranquilo.",
        "{dias} días debiendo {cuanto}. A {quien} ya le sale gratis el descaro.",
        "{quien} lleva {dias} días con {cuanto} pendientes. Ni Hacienda es tan paciente.",
        "Aviso: la deuda de {quien} ({cuanto}) lleva {dias} días y ya tiene derechos adquiridos.",
        "{dias} días. {quien} debe {cuanto} y sigue proponiendo planes caros.",
        "Han pasado {dias} días. {quien}, los {cuanto} no se pagan solos.",
        "{quien}: {cuanto}, {dias} días. Esto ya no es olvido, es carrera profesional."
    )

    private val toque = listOf(
        "Toque enviado. Que tiemble.",
        "Le acaba de vibrar el móvil. De nada.",
        "Aviso lanzado. La pelota está en su tejado.",
        "Recordatorio en camino. Sin sangre.",
        "Le has dado un toque. Diplomacia nivel experto.",
        "Zasca cariñoso enviado. 👋",
        "Ya está avisado. Si no paga, es a mala fe.",
        "Toque dado. El resto es cosa de su conciencia.",
        "Notificación enviada. Que la culpa haga su trabajo.",
        "Recordatorio entregado con todo el cariño y ninguna piedad.",
        "Le ha sonado el móvil. Y sabe perfectamente por qué.",
        "Toque puesto en su bandeja. Ahora toca esperar y desconfiar."
    )

    private val sinGastos = listOf(
        "Aquí no hay ni un gasto. Sospechoso.",
        "Grupo vacío. ¿De verdad no habéis gastado nada? Mentirosos.",
        "Ni un euro apuntado todavía. Empieza tú, valiente.",
        "Esto está más vacío que la cartera de un lunes.",
        "Cero gastos. Cero deudas. Cero diversión.",
        "Añade el primer gasto y que empiece el espectáculo.",
        "Nada por aquí, nada por allá. Aún.",
        "El contador está a cero. Alguien tendrá que pagar algo, ¿no?",
        "Ni un gasto. Este grupo es de los que van a la cena y no pide nada.",
        "Vacío total. Dale al botón gordo de abajo y empieza el circo.",
        "Cero apuntes. La cuenta más sana que verás en tu vida.",
        "Aquí no ha gastado nadie. De momento os creemos."
    )

    private val sinGrupos = listOf(
        "Aún no tienes grupos. Y sin grupo no hay a quién sablear.",
        "Crea un grupo y empieza a llevar las cuentas como un adulto. Un adulto rencoroso.",
        "Ni un grupo. Ni una deuda. Menuda vida aburrida.",
        "Empieza por un grupo: el piso, el viaje o las cañas de los viernes.",
        "Aquí no hay nadie. Invita a esos colegas que nunca pagan.",
        "Primer paso: un grupo. Segundo paso: cobrarles a todos.",
        "Sin grupos no hay morosos. Y sin morosos esto no tiene gracia.",
        "Monta tu primer grupo. El monigote se aburre.",
        "Nada todavía. Un grupo, cuatro colegas y a repartir culpas.",
        "Esto está desierto. Crea un grupo y que empiece la contabilidad creativa."
    )

    private val cabeceraDebo = listOf(
        "Debes {cuanto}. Respira y paga.",
        "En rojo por {cuanto}.",
        "Te toca soltar {cuanto}.",
        "{cuanto} de deuda. Nada que un bizum no arregle.",
        "Debes {cuanto}. El grupo lo sabe todo.",
        "Números rojos: {cuanto}.",
        "{cuanto} en tu contra. Tú sabrás.",
        "Estás a deber {cuanto}. Sin dramas, pero paga.",
        "Saldo: {cuanto} en contra. Se admiten bizums.",
        "Debes {cuanto}. El monigote te está mirando."
    )

    private val cabeceraMeDeben = listOf(
        "Te deben {cuanto}. Ve a por ello.",
        "{cuanto} a tu favor. Bien jugado.",
        "El grupo te debe {cuanto}.",
        "Tienes {cuanto} en la calle.",
        "A favor: {cuanto}. Eres el prestamista del grupo.",
        "{cuanto} pendientes de cobro.",
        "{cuanto} a favor. Cobra antes de la próxima ronda.",
        "Saldo: {cuanto} a tu favor. Bien hecho, banquero.",
        "Te deben {cuanto}. Da un toque, que para eso está el botón.",
        "{cuanto} en la calle. Ni un céntimo perdonado."
    )

    private fun catalogo(momento: Momento): List<String> = when (momento) {
        Momento.DEBES -> debes
        Momento.PAGASTE -> pagaste
        Momento.TE_HAN_PAGADO -> teHanPagado
        Momento.TE_DEBEN -> teDeben
        Momento.GASTO_NUEVO -> gastoNuevo
        Momento.EN_PAZ -> enPaz
        Momento.MOROSO_LEYENDA -> morosoLeyenda
        Momento.TOQUE -> toque
        Momento.SIN_GASTOS -> sinGastos
        Momento.SIN_GRUPOS -> sinGrupos
        Momento.CABECERA_DEBO -> cabeceraDebo
        Momento.CABECERA_ME_DEBEN -> cabeceraMeDeben
    }

    /** Cuantas frases tiene el catalogo entero. Se presume de ello en Ajustes. */
    val total: Int get() = Momento.entries.sumOf { catalogo(it).size }

    /**
     * Una frase de [momento] con los huecos rellenos. Si [semilla] es la misma,
     * sale la misma frase: asi una tarjeta no cambia de coña en cada
     * recomposicion de Compose (que era un mareo).
     */
    fun para(
        momento: Momento,
        quien: String = "",
        cuanto: String = "",
        que: String = "",
        dias: Int = 0,
        semilla: Long = 0L
    ): String {
        val lista = catalogo(momento)
        val indice = if (semilla == 0L) {
            Random.nextInt(lista.size)
        } else {
            ((semilla % lista.size) + lista.size).toInt() % lista.size
        }
        return rellena(lista[indice], quien, cuanto, que, dias)
    }

    private fun rellena(plantilla: String, quien: String, cuanto: String, que: String, dias: Int) =
        plantilla
            .replace("{quien}", quien.ifBlank { "Alguien" })
            .replace("{cuanto}", cuanto.ifBlank { "algo" })
            .replace("{que}", que.ifBlank { "eso" })
            .replace("{dias}", dias.toString())

    /**
     * Rango del moroso segun lo que debe y lo que tarda. Es puro cachondeo, pero
     * ordena el ranking del grupo.
     */
    fun rangoMoroso(deudaCentimos: Long, diasMasVieja: Int): String = when {
        deudaCentimos <= 0L -> "Intachable"
        diasMasVieja <= 2 -> "Despistado"
        diasMasVieja <= 7 -> "Manos de mantequilla"
        diasMasVieja <= 14 -> "Bolsillos cosidos"
        diasMasVieja <= 30 -> "Sablista aficionado"
        diasMasVieja <= 60 -> "Moroso federado"
        diasMasVieja <= 120 -> "Rey del «mañana te lo paso»"
        diasMasVieja <= 240 -> "Agujero negro financiero"
        else -> "Moroso Patrimonio de la Humanidad"
    }

    /** Rango del que siempre pone la tarjeta. */
    fun rangoPagador(pagadoCentimos: Long, gastosPuestos: Int): String = when {
        gastosPuestos == 0 -> "Turista"
        gastosPuestos <= 2 -> "Colaborador"
        gastosPuestos <= 5 -> "Pagafantas oficial"
        gastosPuestos <= 10 -> "El de la tarjeta"
        gastosPuestos <= 20 -> "Cajero automático humano"
        pagadoCentimos >= 100_000L -> "Banco Central del grupo"
        else -> "Mecenas"
    }

    /** Titulo para el ranking de pulgares de un gasto muy votado. */
    fun medallaPulgares(saldo: Int): String? = when {
        saldo >= 5 -> "Gasto del año 🏆"
        saldo >= 3 -> "Aplaudido 👏"
        saldo <= -5 -> "Crimen contra el grupo 💀"
        saldo <= -3 -> "Muy cuestionado 🙃"
        else -> null
    }
}
