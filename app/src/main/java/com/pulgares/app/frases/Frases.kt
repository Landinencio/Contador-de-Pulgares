package com.pulgares.app.frases

import com.pulgares.app.domain.model.Dinero
import kotlin.random.Random

/**
 * El alma de la app. Cada momento tiene su coña: cuando debes, cuando pagas,
 * cuando te deben y cuando alguien lleva tres meses haciendose el sueco.
 *
 * Convenciones de las plantillas:
 *   {quien}   -> nombre del colega
 *   {cuanto}  -> importe ya formateado ("12,50 €")
 *   {pesetas} -> el mismo importe en pesetas ("2.080 pts")
 *   {que}     -> concepto del gasto
 *   {dias}    -> dias que lleva la deuda sin saldar
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
    CABECERA_ME_DEBEN,

    /**
     * Las notificaciones del Cobrador del Frac: un caballero con chistera que
     * recuerda las deudas con una cortesia inquietante. Nunca insulta: amenaza
     * con educacion exquisita.
     */
    COBRADOR,

    /** Al abrir la app: le toca poner la siguiente a otro. {quien} y {cuanto}. */
    SIGUIENTE_RONDA,

    /** Al abrir la app: la siguiente la pones tú. {cuanto} = lo que llevas puesto. */
    SIGUIENTE_RONDA_YO,

    /** El grupo se acaba de llamar de otra manera. */
    NOMBRE_GRUPO,

    /** A un colega le han cambiado el nombre. {quien} = el viejo, {que} = el nuevo. */
    NOMBRE_COLEGA,

    /** Alguien propone renombrar el grupo y toca votar. */
    NOMBRE_A_VOTACION,

    /** Te acaba de llegar un zumbido, como en los tiempos del Messenger. */
    ZUMBIDO
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
        "Tus {cuanto} y mi paciencia se están agotando a la vez.",
        "{cuanto}. O {pesetas}, si lo prefieres en dinero de verdad.",
        "En tus tiempos eso eran {pesetas}, y también las debías.",
        "{quien}, son {pesetas}. Ya te lo pongo en pesetas para que te duela más."
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

    private val siguienteRonda = listOf(
        "La siguiente la paga {quien}, que lleva {cuanto} puestos. Cuentas son cuentas.",
        "Turno de {quien}: con {cuanto} adelantados es el que menos ha rascado.",
        "{quien}, la próxima va a tu cargo. Llevas {cuanto} y no cuela.",
        "Se busca pagador y el algoritmo dice {quien}. {cuanto} puestos, poca hazaña.",
        "Por poderes del reparto: paga {quien}. Va con {cuanto} y con mucha cara.",
        "La ronda que viene la pone {quien}. Sí, {quien}. Con sus {cuanto}.",
        "Aviso oficial: {quien} lleva {cuanto} puestos y le toca aflojar.",
        "{quien} tiene el honor de la siguiente. Un aplauso y la tarjeta.",
        "Ranking del bolsillo: último {quien} ({cuanto}). Le toca.",
        "Que saque la cartera {quien}, que llevamos todos más puesto que él.",
        "{quien}: {cuanto} en toda la historia del grupo. La próxima, tú.",
        "El dedo señala a {quien}. {cuanto} puestos es el récord por abajo.",
        "Siguiente en la lista de la vergüenza: {quien}, con {cuanto}.",
        "Se ruega a {quien} que invite. Sus {cuanto} claman al cielo."
    )

    private val siguienteRondaYo = listOf(
        "La siguiente la pones tú: llevas {cuanto} y eres el que menos ha puesto. Asúmelo.",
        "Malas noticias: te toca. {cuanto} puestos, el farolillo rojo eres tú.",
        "Te ha tocado la china: la próxima la pagas tú, campeón de los {cuanto}.",
        "Con {cuanto} adelantados vas último. La siguiente ronda lleva tu nombre.",
        "Saca la tarjeta: eres el que menos ha puesto ({cuanto}).",
        "Hoy invitas tú. Llevas {cuanto} y el grupo tiene memoria.",
        "El algoritmo te ha mirado y ha dicho: paga. Van {cuanto} tuyos.",
        "Te toca. Sí, a ti. {cuanto} puestos no dan para más excusas.",
        "Tu turno de aflojar: {cuanto} es lo menos de todo el grupo.",
        "Enhorabuena: eres el elegido. Llevas {cuanto} y la próxima es tuya.",
        "Con {cuanto} puestos estás en el podio... por abajo. Invita.",
        "Ha salido tu número. {cuanto} puestos y toca poner la siguiente."
    )

    private val nombreGrupo = listOf(
        "Hecho: el grupo ahora se llama «{que}». Que conste en acta.",
        "Rebautizado. Ahora esto es «{que}» y no se hable más.",
        "«{que}». Cambio guardado, notario ausente.",
        "Listo. El grupo responde a «{que}» desde este mismo instante.",
        "Ya está: «{que}». El anterior nombre pasa a la historia.",
        "Nombre nuevo, mismas deudas: bienvenido a «{que}».",
        "Guardado. «{que}» queda inscrito en el registro de grupos cachondos.",
        "Cambiado a «{que}». Nadie recordará cómo se llamaba antes."
    )

    private val nombreColega = listOf(
        "{quien} pasa a llamarse «{que}». Sin cirugía y sin papeleo.",
        "Hecho: donde decía {quien}, ahora dice «{que}».",
        "{quien} ya es «{que}». Que se acostumbre.",
        "Rebautizado: {quien} → «{que}». Guardado y firmado.",
        "A {quien} le has puesto «{que}». Espero que se lo tome bien.",
        "«{que}» sustituye a {quien}. La deuda, intacta.",
        "{quien} responde ahora a «{que}». El saldo no ha cambiado, tranquilo.",
        "Cambio guardado: {quien} es «{que}» a todos los efectos."
    )

    private val nombreAVotacion = listOf(
        "{quien} quiere llamar a esto «{que}». Se abre urna.",
        "Propuesta sobre la mesa: «{que}», cortesía de {quien}. A votar.",
        "{quien} ya gastó su cambio gratis, así que «{que}» pasa por las urnas.",
        "Referéndum: {quien} propone «{que}». Tú decides.",
        "{quien} insiste en cambiar el nombre. Quiere «{que}». Vota con la cabeza.",
        "A votación: «{que}». Lo propone {quien}, que ya va por el segundo intento."
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
        "Vacío total. Dale al botón gordo de arriba y empieza el circo.",
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

    private val cobrador = listOf(
        "Buenas. Le recuerdo con la máxima elegancia que debe {cuanto}.",
        "He planchado el frac para la ocasión: {cuanto}.",
        "Un caballero no huye de {cuanto}. Un caballero hace un Bizum.",
        "Mi chistera y yo lamentamos comunicarle: {cuanto}.",
        "Sin prisa. Sin pausa. {cuanto}.",
        "Le sigo. A todas partes. Con elegancia. {cuanto}.",
        "El maletín está listo. Solo faltan sus {cuanto}.",
        "Hoy tampoco ha pagado usted sus {cuanto}. Queda anotado en el registro.",
        "{cuanto}. O {pesetas}, si su corazón sigue en 1998.",
        "La puntualidad es la cortesía de los reyes. {cuanto}, majestad.",
        "Su deuda de {cuanto} y yo tomaremos asiento frente a su portal.",
        "Elegancia es pagar {cuanto} antes de que yo silbe.",
        "Llevo {dias} días esperándole con la misma sonrisa: {cuanto}.",
        "No es una amenaza, es una agenda: {cuanto}, hoy.",
        "El grupo me ha contratado. Cobro en frases. Usted debe {cuanto}."
    )

    private val zumbido = listOf(
        "¡ZUMBIDO! {quien} exige tu atención (y probablemente tu dinero).",
        "{quien} te está zumbando. Como en 2006.",
        "El móvil no está roto: es {quien} dándote un zumbido.",
        "{quien} ha invocado el espíritu del Messenger.",
        "Despierta: {quien} te zumba.",
        "{quien} te sacude a distancia. La tecnología es maravillosa.",
        "Zumbido de {quien}. El zumbido nunca murió, solo dormía.",
        "{quien} dice que mires la app. Con cariño, pero que la mires."
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
        Momento.COBRADOR -> cobrador
        Momento.ZUMBIDO -> zumbido
        Momento.SIGUIENTE_RONDA -> siguienteRonda
        Momento.SIGUIENTE_RONDA_YO -> siguienteRondaYo
        Momento.NOMBRE_GRUPO -> nombreGrupo
        Momento.NOMBRE_COLEGA -> nombreColega
        Momento.NOMBRE_A_VOTACION -> nombreAVotacion
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
        /** null = una frase al azar. Con valor, siempre sale la misma. */
        semilla: Long? = null,
        /** Si se pasa, de aqui salen tanto {cuanto} como {pesetas}. */
        centimos: Long? = null
    ): String {
        val lista = catalogo(momento)
        // Antes el centinela era el 0, asi que un hashCode que valiese justo 0
        // hacia bailar esa frase en cada recomposicion.
        val indice = if (semilla == null) {
            Random.nextInt(lista.size)
        } else {
            ((semilla % lista.size) + lista.size).toInt() % lista.size
        }
        val euros = cuanto.ifBlank { centimos?.let { Dinero.formatea(it) } ?: "" }
        val pesetas = centimos?.let { Dinero.formateaPesetas(it) } ?: "unas cuantas pesetas"
        return rellena(lista[indice], quien, euros, pesetas, que, dias)
    }

    private fun rellena(
        plantilla: String,
        quien: String,
        cuanto: String,
        pesetas: String,
        que: String,
        dias: Int
    ) = plantilla
        .replace("{quien}", quien.ifBlank { "Alguien" })
        .replace("{cuanto}", cuanto.ifBlank { "algo" })
        .replace("{pesetas}", pesetas)
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

    /**
     * Los rangos del zumbador, de menor a mayor descaro. Se sube uno **cada
     * tres zumbidos** (petición de los testers: llegar a "acreedor" con tres
     * era demasiado barato), y a partir del último la cosa ya no tiene nombre
     * conocido: se numera y punto.
     */
    private val rangosZumbido = listOf(
        "Toque de cortesía",
        "Recordatorio con sonrisa",
        "Insistencia sana",
        "Ya va en serio",
        "Acreedor",
        "Acreedor federado",
        "Abeja obrera",
        "Avispa con antecedentes",
        "Enjambre",
        "Cobrador aficionado",
        "Cobrador del Frac",
        "Martillo pilón",
        "Sirena de fábrica",
        "Terremoto grado 7",
        "Hacienda en persona",
        "Plaga bíblica",
        "Despertador del apocalipsis",
        "Taladro de las ocho de la mañana",
        "Vecino del piso de arriba",
        "Testigo de Jehová un domingo",
        "Comercial de telefonía",
        "Llamada de un número desconocido",
        "Alarma de coche a las tres",
        "Gaviota en un camping",
        "Grupo de guasap de la familia",
        "Mosquito a oscuras",
        "Cobrador cósmico",
        "Deidad del zumbido"
    )

    /** El nivel de insistencia: uno nuevo cada tres zumbidos, sin techo. */
    fun nivelZumbido(veces: Int): Int = (veces.coerceAtLeast(1) - 1) / 3 + 1

    /** El nombre del rango para ese nivel; pasado el último, se repite el de arriba. */
    fun rangoZumbido(veces: Int): String =
        rangosZumbido[(nivelZumbido(veces) - 1).coerceAtMost(rangosZumbido.lastIndex)]

    /** Lo que se lee en la chapa del cartelón: "×7 · nivel 3 — Insistencia sana". */
    fun chapaZumbido(veces: Int): String {
        val nivel = "nivel ${nivelZumbido(veces)} — ${rangoZumbido(veces)}"
        return if (veces > 1) "×$veces · $nivel" else nivel
    }

    // Sermones para el pesado del zumbido, segun por donde vaya el mes. La
    // nomina manda: a principios no hay excusa, a finales no hay dinero.
    private val sermonPrincipioDeMes = listOf(
        "Y encima recién cobrado. Contrólate, campeón.",
        "Estamos a principios de mes: ese dinero existe. Pero contrólate.",
        "Acabas de cobrar y ya estás zumbando. Respira.",
        "Con la nómina calentita y tú dando por saco. Bonito.",
        "A estas alturas del mes nadie tiene excusa, pero tú tampoco.",
        "Recién pagada la nómina y tú con el dedo en el timbre."
    )

    private val sermonMitadDeMes = listOf(
        "Mitad de mes: territorio neutral. Aún se puede negociar.",
        "Vamos por la mitad del mes, la cosa está regular para todos.",
        "Ni principios ni finales. Zumba con moderación.",
        "Media maratón del mes. Guarda fuerzas para el día 28.",
        "Mitad de mes, mitad de esperanza. Sigue insistiendo con clase."
    )

    private val sermonFinalDeMes = listOf(
        "Entiendo que estamos a finales y no queda un mango. Tranquilo, acabará pagando.",
        "Final de mes: ahí no hay dinero ni buscándolo con perro. Paciencia.",
        "A estas alturas del mes la cartera es un ecosistema desértico. Ya te pagará.",
        "Estamos a finales. Ese que zumbas está comiendo arroz blanco. Ten piedad.",
        "Fin de mes, cuentas en números rojos para todos. Espera al día 1.",
        "Queda nada para la nómina. Aguanta, que el día 1 cobra y le vuelves a zumbar."
    )

    /**
     * Un comentario para el que se pasa zumbando, segun por donde ande el mes.
     * Solo aparece cuando ya lleva unos cuantos ([veces] >= 4, o sea nivel 2):
     * al primer zumbido nadie necesita un sermon.
     */
    fun sermonZumbador(veces: Int, diaDelMes: Int, semilla: Long? = null): String? {
        if (veces < 4) return null
        val lista = when {
            diaDelMes <= 10 -> sermonPrincipioDeMes
            diaDelMes <= 20 -> sermonMitadDeMes
            else -> sermonFinalDeMes
        }
        val indice = if (semilla == null) {
            Random.nextInt(lista.size)
        } else {
            ((semilla % lista.size) + lista.size).toInt() % lista.size
        }
        return lista[indice]
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
