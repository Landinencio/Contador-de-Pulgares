# Contador de Pulgares 👍

Un Tricount **sin publicidad, sin límites y sin piedad**. Android nativo (Kotlin +
Jetpack Compose), offline, gratis para siempre y con el monigote rosa de mascota.

<!-- El logo es el monigote del escritorio de Rubén, redibujado en vectorial. -->

## Por qué existe

Las apps del ramo se han ido estropeando:

- **Splitwise** limita el plan gratis a **3 gastos al día** (2026), mete anuncios
  entre las entradas y cobra ~40 $/año por lo que antes era gratis (gráficas,
  escaneo de recibos, divisas).
- **Tricount** quitó su Premium, pero sigue habiendo quejas de anuncios.

Aquí no hay nada de eso: los datos viven en tu móvil, no hay cuentas, no hay
anuncios y no hay tope de gastos. Y de paso, la app se ríe de ti cuando debes
dinero.

## Qué hace

- **Grupos** de colegas (el piso, el viaje, las cañas de los viernes).
- **Gastos** con tres formas de repartir:
  - *A escote*: entre los que marques, a partes iguales.
  - *Por partes*: el que repitió cuenta doble.
  - *A dedo*: cada uno pone su importe exacto, al céntimo.
- **Plan de pagos**: el mínimo de bizums para dejar el grupo a cero. Rompe los
  triángulos de deuda (si A debe a B y B debe a C, paga A a C directamente).
- **Pulgares** 👍👎: se vota cada gasto. De ahí el nombre. Los gastos muy votados
  se llevan medalla ("Gasto del año") y los muy criticados también
  ("Crimen contra el grupo").
- **Salón de la fama**: los morosos con más de una semana de retraso, con su
  rango — de *Despistado* a *Moroso Patrimonio de la Humanidad*.
- **Frases cachondas** en cada momento: al deber, al pagar, al cobrar, al apuntar
  un gasto y al quedar en paz.
- **Todo en euros y en pesetas.** Cada importe lleva debajo su equivalencia con el
  cambio oficial de 1998 (1 € = 166,386 pts), porque en el grupo se sigue pensando
  en pesetas. Al escribir un gasto lo ves en vivo: «Que son 3.893 pts de las de
  antes».
- **Creador de monigotes** con más de **2,3 billones** de combinaciones, para ti y
  para cada colega del grupo.
- **Confeti** cuando el grupo se queda a cero. Solo entonces.
- **Compartir el grupo** con un código de seis letras: quien lo teclee ve los
  mismos gastos. Sin registro, sin correo y sin cuentas.
- **El Cobrador del Frac** 🎩: un caballero con chistera, monóculo y maletín que
  te recuerda por notificación lo que debes. Elegante y no pesado: ronda una vez
  al día, habla solo si de verdad debes algo, y nunca más de una vez cada dos
  días. Antes de mirar, sincroniza en silencio los grupos compartidos, así se
  entera de los gastos que apuntaron los demás aunque no abras la app. Se
  contrata (y se despide) desde la portada.

### Las frases

184 frases repartidas en 12 momentos. Una muestra:

| Momento | Ejemplo |
|---|---|
| Debes dinero | «¡Paga, perro! 🐕» · «Suelta la mosca, {quien}» · «Vuelva usted mañana, decías. Ya es mañana» |
| Pagas | «Un Lannister siempre paga sus deudas 🦁» · «El que paga descansa. Descansa, campeón» |
| Te deben | «Pagafantas nivel {cuanto}» · «Cajero automático humano» |
| Moroso de leyenda | «{dias} días. A este ritmo, la deuda cumple la mayoría de edad» |
| Todo saldado | «Cuentas claras y chocolate espeso ✅» |

Los rangos de moroso escalan con los días (*Manos de mantequilla*, *Bolsillos
cosidos*, *Sablista aficionado*, *Rey del «mañana te lo paso»*, *Agujero negro
financiero*…) y los de pagador con los gastos que pones (*Pagafantas oficial*,
*El de la tarjeta*, *Cajero automático humano*, *Banco Central del grupo*).

Todo está en [`frases/Frases.kt`](app/src/main/java/com/pulgares/app/frases/Frases.kt);
añadir más es meter una línea en la lista que toque.

### El creador de monigotes

El avatar **no usa ni una imagen**: se dibuja entero con `Canvas` de Compose, en
un espacio virtual de 100×100, así que se ve igual de nítido a 24dp en una lista
que a 210dp en el editor. Once dimensiones:

| Pieza | Variantes | Ejemplos |
|---|---|---|
| Cuerpo | 8 | Alubia, Patata, Huevo, Pera, Croqueta, Churro, Bola, Flan |
| Color | 14 | Rosa chicle, Verde moco, Azul pitufo, Dorado nuevo rico… |
| Ojos | 16 | Saltones, Bizcos, Enamorado, Signos de euro, Espiral, Rayos láser, Robot… |
| Boca | 16 | Sonrisota, Lengua fuera, Colmillos, Cremallera, Chupete, Rechinando… |
| Pelo | 16 | Tres pelos, Tupé, Afro, Cresta, Mullet, Rastas, Calva brillante… |
| Sombrero | 16 | Boina, Corona, Casco de obra, **Cono de tráfico**, Sartén, Txapela… |
| Gafas | 12 | De sol, Gafotas, Antifaz, Monóculo, Nariz de Groucho, De soldador… |
| Pelambrera | 10 | Perilla, Bigotón, Barba de náufrago, Chuletas, De vikingo… |
| Cachivache | 16 | Jarra de birra, Churro, Litrona, **Cartera vacía con polillas**, Calculadora, Pancarta… |
| Detalles | 12 | Sonrojo, Pecas, Ojeras, Tirita, Gota de sudor, Chichón, Purpurina… |
| Fondo | 14 | Lunares, Rayas, Estrellas, Monedas, Billetes volando, Atardecer, Confeti… |

8 × 14 × 16 × 16 × 16 × 16 × 12 × 10 × 16 × 12 × 14 = **2 367 600 721 920**
monigotes distintos. Cada miniatura del editor se dibuja sobre *tu* monigote, no
sobre uno genérico, para que veas cómo te queda la pieza antes de elegirla.

El botón 🎲 saca uno al azar (con los "nada" favorecidos para que no salga
siempre un bicho recargado), y los colegas sin avatar reciben uno estable
generado a partir de su nombre.

## Cómo se ve

Estética de pegatina: borde negro de rotulador, sombra dura desplazada y todo
redondeado. Tipografías [Fredoka](https://fonts.google.com/specimen/Fredoka)
(títulos) y [Baloo 2](https://fonts.google.com/specimen/Baloo+2) (texto), las dos
con licencia OFL — el aviso de licencia va incluido en
[`res/raw/`](app/src/main/res/raw/). Modo claro y oscuro.

## Arquitectura

```
app/src/main/java/com/pulgares/app/
├── domain/
│   ├── model/          Dinero (céntimos en Long), Grupo, Gasto, Reparto, Categoría
│   └── settlement/     Cuentas: saldos + plan de pagos (greedy)
├── data/
│   ├── local/          Room: grupos, colegas, gastos, pagos
│   └── Repositorio     combina las tablas y devuelve el estado ya calculado
├── avatar/             Monigote (config), Pincel (dibujo), piezas por grupos
├── frases/             el catálogo de coñas
└── ui/                 tema, componentes pegatina, pantallas y ViewModel
```

Dos decisiones que conviene no deshacer:

- **El dinero son céntimos en `Long`, nunca `Double`.** Con floats, `0,10 + 0,20`
  no es `0,30` y las cuentas acaban con un descuadre de un céntimo que nadie sabe
  explicar. El reparto garantiza que la suma de las partes es *exactamente* el
  total, y el céntimo que sobra rota según el gasto para que no le toque siempre
  al mismo pringado.
- **El plan de pagos es el algoritmo greedy clásico** (el de Splitwise y
  compañía): se empareja al que más debe con el que más se le debe. No garantiza
  el óptimo teórico (es NP-duro) pero deja como máximo N−1 bizums.
- **Un gasto no se reescribe nunca.** Al editarlo se conservan su fecha y sus
  pulgares, y quien sale del grupo se marca como inactivo en vez de borrarse. Si se
  borrase, sus gastos perderían el nombre y —peor— al editar uno de ellos el
  reparto se recalcularía entre los que quedan, cambiando deudas que el grupo ya
  había cerrado.

## Compilar

Necesita JDK 17 y el SDK de Android (plataforma 35).

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebug
```

Los tests (72, sobre dinero, pesetas, reparto, settlement, frases, avatares,
sincronización y una tanda de regresiones: un test por cada fallo que ya se coló una vez):

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest
```

El CI de GitHub Actions compila los APK en cada push y los publica en la release
`test-apk`: el de release (15 MB, el que conviene instalar) y el de debug (22 MB).
Los dos van firmados con la misma clave, así que se actualizan el uno sobre el otro.

**La clave de firma está en el repo a propósito** (`app/ci-debug.keystore`, con la
contraseña a la vista en `build.gradle.kts`). Es lo que permite que cualquier build
—la tuya o la del CI— genere un APK que se instala encima del anterior sin
desinstalar. Sirve para repartir la app entre colegas y **para nada más**: si algún
día esto va a una tienda, hay que generar una clave de verdad y guardarla fuera del
repositorio.

Y una comprobación que el CI también ejecuta:

```bash
python3 herramientas/comprueba-migraciones.py
```

Los tests de migración de Room necesitan un dispositivo Android y aquí no hay
emulador, así que ese script hace la misma comprobación con SQLite a secas: crea la
base de la versión vieja, le aplica las migraciones de la app y compara columna por
columna con el esquema que Room exporta a `app/schemas/`. Si una migración se
olvidara de algo, la app reventaría al arrancar sobre una base ya existente; esto lo
caza en un segundo. **Al añadir una migración a `BaseDatos.kt`, añádela también a
`MIGRACIONES` en el script.**

### Ver el monigote sin emulador

Este Mac no tiene emulador de Android instalado, así que en `herramientas/` hay dos
ports de la geometría del monigote a `<canvas>` que se abren en el navegador:

- [`vista-monigote.html`](herramientas/vista-monigote.html): la parrilla desnuda de
  formas, ojos, bocas y tocados. Para ajustar proporciones.
- [`vista-app.html`](herramientas/vista-app.html): la presentación completa, con
  las tres pantallas maquetadas y el monigote bailando.

Los dos son un port, no la app: si cambias la anatomía en
[`avatar/Cuerpo.kt`](app/src/main/java/com/pulgares/app/avatar/Cuerpo.kt) hay que
actualizarlos a mano para que sigan sirviendo.

## Compartir un grupo

Un grupo puede vivir solo en tu móvil (por defecto) o compartirse. Al compartirlo
sale un **código de seis caracteres** que se manda por WhatsApp; quien lo teclee
entra y desde entonces los dos móviles ven los mismos gastos. Sin registro, sin
correo y sin contraseñas: el código *es* la invitación.

La primera vez que se abre la app se elige **perfil**: tu nombre y tu monigote.
Es lo que te identifica en todos los grupos — al crear uno entras tú con tu
perfil, y al pedir entrar en otro, tu petición viaja con tu nombre y tu careto.
Nadie bautiza a nadie: cada uno se llama como quiere.

Detalles que importan:

- El alfabeto tiene 31 símbolos, sin `0`, `O`, `1`, `I` ni `L`, que son los que se
  confunden al dictar el código en voz alta. Salen 887 millones de combinaciones.
- **Se entra por solicitud.** Con el código no se entra directamente: le llega una
  petición al dueño del grupo (con el nombre y el monigote del solicitante) y él
  la aprueba o la rechaza desde sus ajustes. Al aprobar puede asignarla a un
  colega que creó a mano —hereda sus gastos— o dejar que entre como alguien
  nuevo; en los dos casos el nombre y el careto son los que eligió el solicitante.
- **El código se puede cambiar.** Sin eso, a quien echas del grupo le basta con
  haberlo apuntado para volver a entrar.
- Al crear un grupo compartido sale un **código de recuperación** que solo ve quien
  lo creó: es lo único que devuelve el mando del grupo si cambias de móvil.
- **Fusionar no necesita resolver conflictos**: los gastos y los bizums llevan su
  identificador desde que se crean y son hechos, no estado, así que juntar dos
  móviles es la unión por identificador. Lo único que se edita (el concepto de un
  gasto, el nombre del grupo) lleva `version`, y gana la más alta: una copia vieja
  que llega tarde se ignora en vez de retroceder el estado.

El backend son una Lambda y una tabla DynamoDB en la cuenta AWS personal, con el
handler en `NininosFit/backend/pulgares/handler.py` y su Terraform en
`NininosFit/infra/pulgares.tf`. El diseño completo está en
[`docs/SINCRONIZACION.md`](docs/SINCRONIZACION.md).

**Sin token, nada de esto existe**: `SYNC_TOKEN` llega por secreto de CI, y si
falta, la app oculta el botón de compartir y funciona igual, entera y local.

## Lo que falta

- [ ] El botón 👉 de dar un toque sigue siendo local (el aviso instantáneo al
      moroso requeriría push de Firebase). En la práctica lo cubre el Cobrador
      del Frac: si el moroso tiene la app y el grupo compartido, su propio
      cobrador le da la ronda diaria.
- [ ] Exportar a CSV y gastos recurrentes (el alquiler, Netflix…).
