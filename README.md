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
- **Creador de monigotes** con más de **2,3 billones** de combinaciones.

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

## Compilar

Necesita JDK 17 y el SDK de Android (plataforma 35).

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebug
```

Los tests (43, sobre dinero, pesetas, reparto, settlement, frases y avatares):

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest
```

El CI de GitHub Actions compila el APK en cada push y lo publica como release
`test-apk`, igual que el resto de apps de casa.

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

## Lo que falta

- [ ] **Compartir grupo entre móviles**. Ahora cada móvil lleva sus cuentas. El
      plan es un endpoint en la cuenta AWS personal (Lambda + DynamoDB + API
      Gateway, el mismo API Gateway que ya sirve a las otras apps de casa) con un
      código de grupo corto para unirse. La app ya tiene los `BuildConfig`
      (`SYNC_URL`, `SYNC_TOKEN`) preparados y se comporta bien sin ellos.
- [ ] **Notificaciones de verdad** para el botón de "dar un toque": hoy el aviso
      se queda en el móvil del que lo manda.
- [ ] Confeti al saldar un grupo (`nl.dionsegijn:konfetti-compose:2.0.5`).
- [ ] Exportar a CSV y gastos recurrentes (el alquiler, Netflix…).
