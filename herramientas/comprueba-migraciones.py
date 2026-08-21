#!/usr/bin/env python3
"""Comprueba que las migraciones de Room dejan la base como Room espera.

Los tests de migración de Room necesitan un dispositivo Android, y aquí no hay
emulador. Pero Room exporta el esquema de cada versión a `app/schemas/`, así que
se puede hacer la comprobación con SQLite a secas: se crea la base de la versión
vieja, se le aplican las migraciones de la app y se compara columna por columna
con el esquema de la versión nueva.

Si una migración se olvida de una columna, la app reventaría al arrancar sobre
una base ya existente con "Migration didn't properly handle...". Esto lo caza
antes, en el CI, y en un segundo.

Al añadir una migración a BaseDatos.kt, añádela también a MIGRACIONES.
"""

from __future__ import annotations

import json
import sqlite3
import sys
from pathlib import Path

ESQUEMAS = Path(__file__).resolve().parent.parent / "app/schemas/com.pulgares.app.data.local.BaseDatos"

# Las mismas sentencias que ejecuta BaseDatos.kt, en orden.
MIGRACIONES: dict[tuple[int, int], list[str]] = {
    (1, 2): [
        "ALTER TABLE colegas ADD COLUMN activo INTEGER NOT NULL DEFAULT 1",
    ],
    (2, 3): [
        "ALTER TABLE gastos ADD COLUMN version INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE pagos ADD COLUMN version INTEGER NOT NULL DEFAULT 0",
    ],
    (3, 4): [
        "ALTER TABLE grupos ADD COLUMN remotoId TEXT DEFAULT NULL",
        "ALTER TABLE grupos ADD COLUMN version INTEGER NOT NULL DEFAULT 0",
    ],
    (4, 5): [
        "ALTER TABLE gastos ADD COLUMN borrado INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE pagos ADD COLUMN borrado INTEGER NOT NULL DEFAULT 0",
    ],
    (5, 6): [
        "ALTER TABLE colegas ADD COLUMN version INTEGER NOT NULL DEFAULT 0",
    ],
}

# Filas de ejemplo por versión de partida, para comprobar que los datos que ya
# existían sobreviven (y con qué valor se quedan en las columnas nuevas).
DATOS_DE_PRUEBA: dict[int, list[str]] = {
    1: [
        "INSERT INTO grupos VALUES ('g1','El piso','🏠',0,NULL)",
        "INSERT INTO colegas VALUES ('c1','g1','Ana',NULL,0,0)",
        "INSERT INTO colegas VALUES ('c2','g1','Yo',NULL,1,1)",
    ],
    2: [
        "INSERT INTO grupos VALUES ('g1','El piso','🏠',0,NULL)",
        "INSERT INTO colegas VALUES ('c1','g1','Ana',NULL,0,0,1)",
        "INSERT INTO gastos VALUES ('x1','g1','Cañas',1000,'c1',0,'BIRRAS','escote:c1',NULL,'','')",
        "INSERT INTO pagos VALUES ('p1','g1','c1','c2',500,0,NULL)",
    ],
    3: [
        "INSERT INTO grupos VALUES ('g1','El piso','🏠',0,NULL)",
        "INSERT INTO colegas VALUES ('c1','g1','Ana',NULL,0,0,1)",
        "INSERT INTO gastos VALUES ('x1','g1','Cañas',1000,'c1',0,'BIRRAS','escote:c1',NULL,'','',0)",
    ],
    4: [
        "INSERT INTO grupos VALUES ('g1','El piso','🏠',0,NULL,NULL,0)",
        "INSERT INTO gastos VALUES ('x1','g1','Cañas',1000,'c1',0,'BIRRAS','escote:c1',NULL,'','',0)",
        "INSERT INTO pagos VALUES ('p1','g1','c1','c2',500,0,NULL,0)",
    ],
}

# Lo que tiene que valer cada columna nueva en las filas que ya existían.
VALORES_ESPERADOS: dict[tuple[int, int], list[tuple[str, str, object]]] = {
    (1, 2): [("colegas", "activo", 1)],
    (2, 3): [("gastos", "version", 0), ("pagos", "version", 0)],
    # remotoId se queda NULL: un grupo que ya existía no está compartido.
    (3, 4): [("grupos", "remotoId", None), ("grupos", "version", 0)],
    # Nada de lo que ya existía está borrado.
    (4, 5): [("gastos", "borrado", 0), ("pagos", "borrado", 0)],
}


def esquema(version: int) -> dict:
    fichero = ESQUEMAS / f"{version}.json"
    if not fichero.exists():
        raise SystemExit(f"No hay esquema exportado para la versión {version} ({fichero})")
    return json.loads(fichero.read_text())["database"]


def crea(con: sqlite3.Connection, version: int) -> None:
    for entidad in esquema(version)["entities"]:
        con.execute(entidad["createSql"].replace("${TABLE_NAME}", entidad["tableName"]))
        for indice in entidad.get("indices", []):
            con.execute(indice["createSql"].replace("${TABLE_NAME}", entidad["tableName"]))


def columnas(con: sqlite3.Connection, tabla: str) -> list[tuple]:
    return [(f[1], f[2].upper(), f[3]) for f in con.execute(f"PRAGMA table_info(`{tabla}`)")]


def comprueba(desde: int, hasta: int) -> list[str]:
    fallos: list[str] = []

    real = sqlite3.connect(":memory:")
    crea(real, desde)
    for sentencia in DATOS_DE_PRUEBA.get(desde, []):
        real.execute(sentencia)
    for sentencia in MIGRACIONES[(desde, hasta)]:
        real.execute(sentencia)

    esperado = sqlite3.connect(":memory:")
    crea(esperado, hasta)

    tablas = [e["tableName"] for e in esquema(hasta)["entities"]]
    for tabla in tablas:
        obtenidas = columnas(real, tabla)
        deseadas = columnas(esperado, tabla)
        if obtenidas != deseadas:
            fallos.append(
                f"La tabla «{tabla}» no queda como Room espera tras migrar {desde}→{hasta}:\n"
                f"    tras migrar: {obtenidas}\n"
                f"    esquema {hasta}:  {deseadas}"
            )

    # Los valores solo se miran si las columnas cuadran: si falta una, el error
    # que importa es el de arriba y consultarla solo daría un fallo derivado.
    if fallos:
        return fallos

    for tabla, columna, valor in VALORES_ESPERADOS.get((desde, hasta), []):
        try:
            distintos = real.execute(
                f"SELECT COUNT(*) FROM `{tabla}` WHERE `{columna}` IS NOT ?", (valor,)
            ).fetchone()[0]
        except sqlite3.OperationalError as error:
            fallos.append(f"No se puede comprobar «{tabla}.{columna}» tras migrar {desde}→{hasta}: {error}")
            continue
        if distintos:
            fallos.append(
                f"En «{tabla}» hay {distintos} filas de antes de la migración "
                f"donde «{columna}» no vale {valor!r}: los datos que ya existían "
                f"no se quedan como deben."
            )

    return fallos


def main() -> int:
    version_actual = max(int(f.stem) for f in ESQUEMAS.glob("*.json"))
    saltos = sorted(MIGRACIONES)

    esperados = [(v, v + 1) for v in range(1, version_actual)]
    if saltos != esperados:
        print(f"✗ Faltan migraciones: hay esquemas hasta la v{version_actual} "
              f"y migraciones {saltos}; se esperaban {esperados}.")
        return 1

    fallos: list[str] = []
    for desde, hasta in saltos:
        problemas = comprueba(desde, hasta)
        if problemas:
            fallos += problemas
        else:
            print(f"✓ Migración {desde}→{hasta}: la base queda como Room espera")

    if fallos:
        print()
        for fallo in fallos:
            print(f"✗ {fallo}")
        return 1

    print(f"✓ {len(saltos)} migración(es) comprobada(s) hasta la v{version_actual}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
