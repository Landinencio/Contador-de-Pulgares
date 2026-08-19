package com.pulgares.app.frases

import kotlin.random.Random

/**
 * El fiscal anticorrupción del grupo.
 *
 * Cuando alguien apunta un gasto de concepto sospechoso, la app suelta un
 * chascarrillo con la actualidad española de los últimos treinta años, que da
 * para un rato. Todo es sátira de casos y tramas de dominio público; ninguna
 * frase acusa a nadie de nada, que aquí se viene a repartir cañas, no querellas.
 *
 * Cómo funciona: el concepto se normaliza (minúsculas, sin tildes, sin signos) y
 * se parte en palabras. Una familia salta si alguna de sus claves aparece **como
 * palabra entera** — así "cocacola" no cuenta como "coca" — salvo que el
 * concepto contenga una de sus excepciones.
 */
object Chascarrillos {

    private class Familia(
        val claves: List<String>,
        val frases: List<String>,
        /** Si el concepto contiene esto, la familia no salta. */
        val excepciones: List<String> = emptyList()
    )

    private val familias = listOf(
        // ---- la noche y sus aledaños ----
        Familia(
            claves = listOf(
                "puticlub", "club", "travestis", "travesti", "alterne", "whiskeria",
                "senoritas", "barra americana", "sauna", "masajes", "masaje",
                "relaciones publicas", "compania", "chicas", "acompanantes"
            ),
            excepciones = listOf("club de lectura", "club de futbol", "club de padel", "masaje deportivo"),
            frases = listOf(
                "Ábalos estaría orgulloso.",
                "Esto en un informe de la UCO figura como «gastos de representación».",
                "El Tito Berni te habría conseguido mesa.",
                "Concepto oficial: reunión institucional de trabajo.",
                "Ojo, que esto se factura a nombre de la asesoría del cuñado.",
                "Y luego dirás que lo pagaste con dinero de tu bolsillo. Ya.",
                "Este gasto no aparecerá en ninguna declaración de bienes.",
                "Que quede claro: tú ibas a hablar de infraestructuras."
            )
        ),

        // ---- sustancias variadas ----
        Familia(
            claves = listOf(
                "coca", "cocaina", "farlopa", "perico", "maria", "porros", "porro",
                "costo", "hachis", "mdma", "pastillas", "tripi", "ketamina", "cristal",
                "polvo", "raya", "rayas"
            ),
            excepciones = listOf("coca cola", "cocacola", "coca-cola", "maria luisa", "polvorones", "polvo de hornear"),
            frases = listOf(
                "¿Qué jugador se perdió la Gürtel contigo?",
                "Esto lo declaras como material de oficina, claro.",
                "Más blanco que una contabilidad paralela.",
                "Y el IVA de esto, ¿cómo lo desgravas?",
                "Esto en los papeles va como «suministros varios».",
                "Se apunta en la libreta, pero en la otra libreta.",
                "Precio de amigo, comisión de intermediario aparte.",
                "Esta línea del Excel se borra antes de la auditoría."
            )
        ),

        // ---- la comilona de las de antes ----
        Familia(
            claves = listOf(
                "marisco", "mariscada", "bogavante", "chuleton", "txuleta", "cigalas",
                "percebes", "jamon", "michelin", "degustacion", "menu degustacion",
                "puros", "puro", "champan", "vega sicilia", "gran reserva"
            ),
            frases = listOf(
                "Las tarjetas black se inventaron para momentos así.",
                "Esto en Marbella tuvo su propio sumario.",
                "Comida de trabajo, evidentemente. Cuatro horas de trabajo.",
                "Y de postre, una comisión.",
                "Este ticket lo firma el consejo de administración.",
                "Sobrecoste, pero de bogavante.",
                "Con lo que ha costado esto se pagaba un polideportivo.",
                "Gasto de representación. Representabas al grupo comiendo."
            )
        ),

        // ---- la obra y sus milagros ----
        Familia(
            claves = listOf(
                "obra", "obras", "reforma", "albanil", "fontanero", "electricista",
                "cemento", "escombros", "presupuesto", "materiales", "chapuza", "derribo"
            ),
            frases = listOf(
                "El 3% va aparte, como manda la tradición.",
                "Adjudicación directa, sin concurso y sin preguntas.",
                "El presupuesto inicial era la mitad. Como siempre.",
                "Obra pública de manual: empieza barata y acaba en el juzgado.",
                "Modificado de obra número cuatro. Y vamos a mitad.",
                "Esto lo lleva una empresa de un primo, ¿verdad?",
                "Sobrecoste del 40% y aún no ha empezado.",
                "La UTE que lo hizo se disolvió al cobrar. Curioso."
            )
        ),

        // ---- viajes de los que salen en los periódicos ----
        Familia(
            claves = listOf(
                "yate", "crucero", "resort", "spa", "business", "primera clase",
                "jet", "helicoptero", "suite", "limusina", "cayo", "safari"
            ),
            frases = listOf(
                "Esto lo paga una fundación sin ánimo de lucro.",
                "Viaje de negocios. El negocio era el viaje.",
                "A nombre de una sociedad instrumental, mejor.",
                "Consta como misión comercial en el extranjero.",
                "Y el vuelo lo puso un empresario amigo, seguro.",
                "Esto en su día dio para varios titulares y una infanta.",
                "Cargado a la cuenta de gastos diversos. Muy diversos."
            )
        ),

        // ---- el dinero que no le gusta la luz ----
        Familia(
            claves = listOf(
                "efectivo", "cash", "sobre", "sobres", "negro", "sin factura",
                "sin iva", "mano", "billetes", "metalico", "maletin"
            ),
            excepciones = listOf("sobre la mesa", "cafe negro", "chocolate negro", "viernes negro"),
            frases = listOf(
                "En sobres, como Dios manda.",
                "La caja B te manda saludos.",
                "Sin factura no hay rastro, y sin rastro no hay reproche. Decían.",
                "Esto en un juzgado se llama «contabilidad paralela».",
                "Anotado con una letra, una fecha y ninguna explicación.",
                "Dinero en metálico y ninguna pregunta: el sistema perfecto.",
                "Se entrega en un parking, como toda la vida.",
                "Y esto de dónde salió: la pregunta de todos los sumarios."
            )
        ),

        // ---- geografía fiscal creativa ----
        Familia(
            claves = listOf(
                "andorra", "suiza", "panama", "gibraltar", "luxemburgo", "delaware",
                "cayman", "malta", "bahamas", "offshore"
            ),
            frases = listOf(
                "Los papeles de Panamá te guardan un hueco.",
                "Andorra: ese pueblecito entrañable lleno de bancos.",
                "Suiza no es un país, es un plan de pensiones.",
                "Esto pasa por tres sociedades antes de llegar al bar.",
                "Estructura societaria de una sola persona y cinco países.",
                "La amnistía fiscal cubría esto, tranquilo."
            )
        ),

        // ---- el concepto que no dice nada ----
        Familia(
            claves = listOf(
                "varios", "cosas", "movidas", "historias", "otros", "no se",
                "gastos", "misc", "varias cosas", "lo de siempre", "eso"
            ),
            frases = listOf(
                "Concepto «varios»: así han empezado todas las tramas de este país.",
                "Un poco más de detalle y esto pasaba la auditoría.",
                "«Varios» es lo que se pone cuando no se puede poner lo que fue.",
                "Este apunte tiene menos detalle que una declaración de bienes.",
                "Justificación documental: ninguna. Estupendo.",
                "El instructor te va a pedir el desglose de esto."
            )
        ),

        // ---- la emergencia y sus intermediarios ----
        Familia(
            claves = listOf(
                "mascarillas", "mascarilla", "test", "gel", "hidroalcoholico",
                "epi", "guantes", "urgente", "emergencia"
            ),
            frases = listOf(
                "Contrato de emergencia: sin concurso y a precio de oro.",
                "La comisión del intermediario ya va incluida.",
                "Compra urgente a una empresa creada la semana pasada.",
                "El proveedor no había vendido esto nunca. Detalle menor.",
                "Y el pago, por adelantado. Faltaría más."
            )
        ),

        // ---- el juego y sus premios milagrosos ----
        Familia(
            claves = listOf(
                "casino", "apuestas", "bingo", "tragaperras", "ruleta", "poker",
                "loteria", "quiniela", "primitiva", "rasca"
            ),
            frases = listOf(
                "Esto luego se justifica con un premio de lotería, como todos.",
                "Qué suerte tienes últimamente con los décimos, ¿eh?",
                "Ganancia declarada: la del boleto que te enseñó tu cuñado.",
                "El azar es el mejor testaferro que existe.",
                "Y encima ganaste. Igual que aquel tesorero."
            )
        ),

        // ---- el palco, la caza y las buenas relaciones ----
        Familia(
            claves = listOf(
                "palco", "vip", "entradas", "abono", "caza", "monteria", "coto",
                "toros", "golf", "green fee"
            ),
            frases = listOf(
                "El palco es para relaciones institucionales, no para ver el partido.",
                "Invitación de cortesía. La cortesía la cobras luego.",
                "Esto no es un regalo, es una «atención comercial».",
                "En el coto se cierran más contratos que en el registro.",
                "Y el que te invitó tenía un expediente en marcha, qué casualidad."
            )
        ),

        // ---- la familia, esa constructora ----
        Familia(
            claves = listOf("cunado", "primo", "sobrino", "hermano", "suegro", "yerno"),
            frases = listOf(
                "Empresa familiar, adjudicación familiar.",
                "El hermano de alguien también facturaba, siempre.",
                "Contratado por su valía. La valía es el apellido.",
                "El cuñado presupuesta caro pero factura sin IVA.",
                "Esto lo mira mi primo, que entiende. De cobrar, entiende."
            )
        )
    )

    /**
     * El chascarrillo que le toca a ese concepto, o null si el gasto es honrado.
     * Con [semilla] (el id del gasto, por ejemplo) la frase no cambia nunca, que
     * si no baila en cada recomposición y pierde la gracia.
     */
    fun para(concepto: String, semilla: Long? = null): String? {
        val limpio = normaliza(concepto)
        if (limpio.isBlank()) return null
        val palabras = limpio.split(" ").filter { it.isNotBlank() }.toSet()

        val familia = familias.firstOrNull { familia ->
            if (familia.excepciones.any { it in limpio }) return@firstOrNull false
            familia.claves.any { clave ->
                if (" " in clave) clave in limpio else clave in palabras
            }
        } ?: return null

        val indice = if (semilla == null) {
            Random.nextInt(familia.frases.size)
        } else {
            ((semilla % familia.frases.size) + familia.frases.size).toInt() % familia.frases.size
        }
        return familia.frases[indice]
    }

    /** Minúsculas, sin tildes y sin signos: "¡Coca-Cola!" -> "coca cola". */
    private fun normaliza(bruto: String): String {
        val sinTildes = bruto.lowercase().map { letra ->
            when (letra) {
                'á', 'à', 'ä', 'â' -> 'a'
                'é', 'è', 'ë', 'ê' -> 'e'
                'í', 'ì', 'ï', 'î' -> 'i'
                'ó', 'ò', 'ö', 'ô' -> 'o'
                'ú', 'ù', 'ü', 'û' -> 'u'
                'ñ' -> 'n'
                'ç' -> 'c'
                else -> letra
            }
        }.joinToString("")
        return sinTildes
            .map { if (it.isLetterOrDigit()) it else ' ' }
            .joinToString("")
            .replace(Regex(" +"), " ")
            .trim()
    }
}
