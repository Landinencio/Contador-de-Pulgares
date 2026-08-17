# Compartir un grupo entre móviles

Diseño de la fase 2: que varios móviles lleven las mismas cuentas. **Nada de esto
está implementado ni desplegado todavía** — es la receta para cuando se monte, y
está calcada del patrón que ya funciona en Entre Páginas y NininosFit, con dos
fallos de aquellas corregidos.

## Cómo se entra a un grupo

Un código de **seis caracteres** que se manda por WhatsApp. Sin registro, sin
correo, sin contraseña: el código *es* la invitación.

```
Alfabeto (31 símbolos): A B C D E F G H J K M N P Q R S T U V W X Y Z 2 3 4 5 6 7 8 9
```

Sin `0`, `O`, `1`, `I` ni `L`, que son los que se confunden al dictarlos en voz
alta o al leerlos de una captura. Son 31⁶ = **887.503.681** combinaciones.

Cada grupo tiene además un **código de recuperación de diez caracteres** que solo
ve quien lo creó. Sirve para el caso "he cambiado de móvil": sin él, un móvil
nuevo no puede demostrar que es el mismo dueño y el grupo se pierde. Esto en
Entre Páginas se añadió después y hubo que generarlo a posteriori para los clubes
antiguos; aquí va desde el primer día.

### Los dos fallos que no repetimos

1. **Generar con `secrets`, no con `random`.** Entre Páginas usa
   `random.choices()`, que es un Mersenne Twister predecible, y lo usa incluso
   para el código de recuperación que transfiere la administración del club. Aquí
   se usa `secrets.choice()`, que sale del generador criptográfico del sistema.
2. **Comprobar colisiones al crear.** Entre Páginas hace `put_item` directo y su
   función de unirse coge el primer resultado que salga, así que dos grupos con el
   mismo código mandarían a la gente al grupo equivocado. Aquí se copia el bucle de
   NininosFit: hasta cinco intentos consultando el índice, y si no hay hueco se
   responde 503 en vez de crear un duplicado silencioso.

Y una carencia del original que sí tapamos: **poder cambiar el código**. Hoy en
Entre Páginas, quien se apuntó el código puede volver a entrar aunque lo hayan
echado, porque el código no rota nunca y no hay lista negra. En los ajustes del
grupo habrá un botón para regenerarlo.

## Datos

Una sola tabla DynamoDB, `PAY_PER_REQUEST` (céntimos al mes a escala de amigos):

| PK | SK | Para qué |
|---|---|---|
| `GRUPO#<id>` | `META` | nombre, emoji, **`codigo`**, **`codigoRecuperacion`**, `duenoUid`, creado |
| `GRUPO#<id>` | `MIEMBRO#<uid>` | nombre, avatar, `GSI1PK=USER#<uid>` para "mis grupos" |
| `GRUPO#<id>` | `GASTO#<id>` | el gasto entero, con su reparto |
| `GRUPO#<id>` | `PAGO#<id>` | los bizums registrados |

Dos índices:

- **`porCodigo`** (hash `codigo`, `projection_type = ALL`): es *sparse* a
  propósito — solo el item `META` lleva el atributo `codigo`, así que el índice
  tiene exactamente una fila por grupo y unirse se resuelve con una consulta que
  ya devuelve todo lo necesario sin volver a la tabla base.
- **`gsi1`** (hash `GSI1PK`, range `GSI1SK`): los grupos de un dispositivo.

## Fusionar sin conflictos

Aquí está la ventaja de cómo está hecho el dominio: **cada gasto y cada pago ya
tienen un identificador único** (`UUID`), y son inmutables una vez apuntados. Así
que sincronizar es la unión de dos conjuntos por identificador. No hay que
resolver conflictos, no hace falta un reloj compartido y da igual el orden en que
lleguen las cosas: dos personas apuntando gastos a la vez no pueden pisarse.

Lo único que necesita cuidado son los datos del grupo que **sí** se editan
(nombre, emoji, la lista de colegas, el avatar de cada uno). Para eso, el mismo
truco de NininosFit: un `version` por documento y

```
ConditionExpression = "attribute_not_exists(version) OR version < :v"
```

de forma que una escritura vieja que llegue tarde se ignora en silencio en vez de
retroceder el estado.

Los votos de pulgares son un conjunto de identificadores, así que también se
fusionan por unión — con la salvedad de que retirar un voto es un borrado, y un
borrado no se puede distinguir de "aún no ha llegado". Para los pulgares se acepta
que gane el último que escriba: es un chiste, no una cuenta.

## Autenticación

La misma que el resto de las apps de casa, que ya está probada:

- `x-token`: el token familiar compartido, que llega al APK por secreto de CI
  (`CDP_SYNC_TOKEN`). Es el único gate real.
- `x-pulgares-uid`: un UUID anónimo por dispositivo, generado la primera vez y
  guardado en DataStore. No identifica a nadie; sirve para saber en qué grupos
  está este móvil y para atribuir el nombre.

Cognito se probó en Diario Luna y se descartó: para algo doméstico no compensa
obligar a registrarse con correo y contraseña.

## Rutas

```
POST /pulgares/crear       -> {grupoId, codigo, codigoRecuperacion}
POST /pulgares/unirse      -> {codigo, nombre, avatar} -> el grupo entero
POST /pulgares/recuperar   -> {codigo, recuperacion, nombre} -> recupera la propiedad
POST /pulgares/rotar       -> nuevo codigo (solo el dueño)
GET  /pulgares/mios        -> los grupos de este uid
GET  /pulgares/grupo       -> ?id= : gastos, pagos y miembros
POST /pulgares/sube        -> gastos y pagos nuevos de este móvil (unión por id)
```

`unirse` tiene que ser **idempotente**: volver a entrar solo refresca el nombre y
el avatar, no da error. Y debe **reintentar una vez tras una espera corta** si el
código no aparece: el índice `porCodigo` es eventualmente consistente, así que
crear un grupo y que otro se una en el mismo segundo puede dar un 404 falso.

El código se normaliza igual en cliente y servidor: recortar espacios, mayúsculas
y quitar guiones, para que valga pegar `paga-42` tal cual.

## Compartir la invitación

Un enlace `pulgares://unirse?c=PAGA42`, con el mismo detalle que Entre Páginas: si
algún día hubiera que meter una llave en el enlace, va **detrás del `#`**, porque
el fragmento es la única parte de una URL que el navegador no manda al servidor.

Y al pegar, no exigir que el texto *sea* el código: buscarlo dentro del mensaje.
WhatsApp no hace pulsables los esquemas propios, así que la gente pega el mensaje
entero.

## Notificaciones

El botón 👉 de dar un toque hoy se queda en el móvil que lo pulsa. Para que le
vibre al moroso hacen falta notificaciones push, y eso significa Firebase Cloud
Messaging, es decir, una dependencia de Google en una app que ahora mismo no tiene
ninguna. Alternativa sin Firebase: un `WorkManager` que mire cada cierto rato (como
el `AvisosWorker` de Entre Páginas, cada 15 minutos) y lance una notificación
local. Llega tarde, pero llega, y no hay que meter a Google en esto.

## Coste

Lambda, API Gateway y DynamoDB a escala de amigos entran en la capa gratuita o
cuestan céntimos. No hay Bedrock aquí, así que esto no mueve la factura: el
presupuesto de 20 € de la cuenta sigue tan tranquilo.
