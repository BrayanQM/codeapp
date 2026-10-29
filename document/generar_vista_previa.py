#!/usr/bin/env python3
"""Genera vista-previa.html: la presentacion real + estado de cada imagen.

Uso:  python3 generar_vista_previa.py
Salida: vista-previa.html  (abrir en el navegador)
"""
import html
import os
import re

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(BASE, "presentacion.html")
DST = os.path.join(BASE, "vista-previa.html")

# nombre de imagen -> (archivo del proyecto, lineas, para donde va)
GUIA = {
    "consola_firebase_url.png": (
        "Consola Firebase > Pestana Datos",
        "captura del navegador",
        "Muestra la URL de la base y los 3 productos listados",
    ),
    "codigo_build_gradle.png": (
        "app/build.gradle.kts",
        "lineas 8-13 y 30-33",
        "localProperties + buildConfigField FIREBASE_DB_URL",
    ),
    "codigo_rutas.png": (
        "app/.../data/FirebaseApi.kt",
        "lineas 49-54",
        "rutaRaiz() y ruta(id): el .json obligatorio",
    ),
    "codigo_http.png": (
        "app/.../data/FirebaseApi.kt",
        "lineas 56-71",
        "peticion(): openConnection, requestMethod, timeouts",
    ),
    "codigo_corrutina.png": (
        "app/.../data/ProductoRepository.kt",
        "lineas 95-98",
        "conHilo() con withContext(Dispatchers.IO)",
    ),
    "codigo_errores.png": (
        "app/.../data/FirebaseApi.kt",
        "lineas 88-96",
        "explicar(): 401/403 y 404 con su mensaje",
    ),
    "consola_security_rules.png": (
        "Consola Firebase > Pestana Reglas",
        "captura del navegador",
        "Reglas de seguridad publicadas",
    ),
    "diagrama_flujo.png": ("generado", "ya listo", "diagrama App -> HTTP -> Firebase"),
    "logo_uf.png": ("logo UNIFRANZ", "ya listo", "usado en portada y cierre"),
    "app_lista.png": ("captura real del telefono", "ya listo", "pantalla de productos"),
    "app_formulario.png": ("captura real del telefono", "ya listo", "formulario de alta"),
    "app_confirmacion.png": ("captura real del telefono", "ya listo", "dialogo de eliminar"),
    "app_y_firebase.png": ("compuesta", "pendiente", "app + consola lado a lado"),
    "codigo_corrutina2.png": (
        "app/.../ui/ProductoController.kt",
        "lineas 69-76",
        "lanzar() con alcance.launch (opcional)",
    ),
}


defImg = "::"


def existe(nombre):
    return os.path.isfile(os.path.join(BASE, "imagenes", nombre))


def main():
    with open(SRC, encoding="utf-8") as f:
        doc = f.read()

    estilo = re.search(r"<style>.*?</style>", doc, re.S)
    estilo = estilo.group(0) if estilo else ""

    slides = re.findall(r"<section class=\"slide.*?</section>", doc, re.S)

    faltantes, listas = [], []

    bloques = []
    for i, s in enumerate(slides, start=1):
        titulo = re.search(r"<h[12][^>]*>(.*?)</h[12]>", s, re.S)
        titulo = re.sub(r"<[^>]+>", "", titulo.group(1)).strip() if titulo else "Portada"
        numero = re.search(r'<span class="slide-number">(\d+)</span>', s)
        numero = numero.group(1) if numero else str(i)

        # ---- reemplazar imagenes faltantes por un bloque de aviso ----
        def sub(m):
            nombre = os.path.basename(m.group(1))
            if existe(nombre):
                listas.append(nombre)
                return m.group(0)
            faltantes.append(nombre)
            g = GUIA.get(nombre, ("-", "-", ""))
            return (
                '<div class="falta">'
                '<div class="falta-titulo">FALTA ESTA IMAGEN</div>'
                f'<div class="falta-nombre">{html.escape(nombre)}</div>'
                f'<div class="falta-dato">Archivo: {html.escape(g[0])}</div>'
                f'<div class="falta-dato">Ubicacion: {html.escape(g[1])}</div>'
                f'<div class="falta-dato">Que debe verse: {html.escape(g[2])}</div>'
                "</div>"
            )

        s = re.sub(r'<img src="([^"]+)"[^>]*>', sub, s)

        # quitar la nota auxiliar dentro de la diapositiva
        s = re.sub(r'<p class="nota">.*?</p>', "", s, flags=re.S)

        bloques.append(
            f'<div class="envoltorio"><div class="etiqueta">Diapositiva {numero} &mdash; {html.escape(titulo)}</div>'
            f'<div class="marco">{s}</div></div>'
        )

    total = len(set(listas) | set(faltantes))
    pct = int(100 * len(set(listas)) / total) if total else 0

    # ---- tabla de captura ----
    pendientes_orden = [f for f in sorted(set(faltantes))]
    filas = ""
    for n in pendientes_orden:
        g = GUIA.get(n, ("-", "-", ""))
        filas += (
            "<tr>"
            f'<td><code>{html.escape(n)}</code></td>'
            f"<td>{html.escape(g[0])}</td>"
            f"<td><strong>{html.escape(g[1])}</strong></td>"
            f"<td>{html.escape(g[2])}</td>"
            "</tr>"
        )

    tablero = f"""
  <div class="tablero">
    <h1>Vista previa de la presentacion</h1>
    <div class="progreso">
      <div class="barra"><div class="relleno" style="width:{pct}%"></div></div>
      <div class="cuenta">{len(set(listas))} de {total} imagenes listas ({pct}%)</div>
    </div>
    <p class="ayuda">Las cajas rojas son imagenes que todavia no tomaste. Abri este archivo
    en el navegador y las vas viendo aparecer a medida que las agregues a la carpeta
    <code>imagenes/</code>. Recarga la pagina (F5) para actualizarlas.</p>
    <h2>Te falta tomar estas {len(pendientes_orden)} capturas</h2>
    <table>
      <tr><th>Nombre del archivo</th><th>Donde mirarlo</th><th>Lineas</th><th>Que debe verse</th></tr>
      {filas}
    </table>
    <h2>Como se toman</h2>
    <ol>
      <li>En Android Studio: <code>Ctrl+P</code>, escribi el nombre del archivo, <code>Enter</code>.</li>
      <li>Pose el cursor en la linea de inicio y presiona <code>Shift+Alt+flecha abajo</code>
          hasta la linea de fin.</li>
      <li>Copia con <code>Ctrl+C</code> y pega en un editor o en Paint/WPS.</li>
      <li>Captura la zona <strong>con los numeros de linea visibles</strong>: eso prueba
          que el codigo es real.</li>
      <li>Guarda con el nombre exacto de la primera columna, dentro de la carpeta
          <code>imagenes/</code>.</li>
    </ol>
  </div>
"""

    salida = f"""<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Vista previa - Conectar Firebase a tu APK Android</title>
<style>
{estilo}
body {{ background:#0f0f1a; }}

.tablero {{ max-width:1100px; margin:0 auto; padding:50px 40px 20px; }}
.tablero h1 {{ font-size:2em; margin-bottom:8px; }}
.tablero h2 {{ font-size:1.3em; margin:30px 0 10px; }}
.ayuda {{ color:#cbd5e0; margin:12px 0; line-height:1.7; }}
.progreso {{ margin:20px 0; }}
.barra {{ height:16px; background:#16213e; border-radius:8px; overflow:hidden; border:1px solid #6c3fc5; }}
.relleno {{ height:100%; background:linear-gradient(90deg,#6c3fc5,#a3e635); }}
.cuenta {{ margin-top:8px; color:#a3e635; font-weight:bold; font-size:1.05em; }}
.tablero table {{ font-size:0.95em; }}
.tablero code {{ font-size:0.9em; }}

.envoltorio {{ margin:0 auto 46px; max-width:1280px; }}
.etiqueta {{ background:#0f0f1a; color:#a3e635; font-weight:bold; padding:10px 18px;
  font-size:1.05em; border-left:5px solid #a3e635; }}
.marco {{ border:2px solid #6c3fc5; }}

.falta {{ border:3px dashed #e11d48; border-radius:12px; background:rgba(225,29,72,.10);
  padding:22px; text-align:center; color:#fda4af; font-family:'Courier New',monospace; }}
.falta-titulo {{ color:#e11d48; font-weight:bold; font-size:1.1em; margin-bottom:10px; }}
.falta-nombre {{ color:#fbbf24; font-size:1.15em; font-weight:bold; margin-bottom:12px;
  word-break:break-all; }}
.falta-dato {{ font-size:0.88em; line-height:1.7; }}
</style>
</head>
<body>
{tablero}
{"".join(bloques)}
</body>
</html>
"""

    with open(DST, "w", encoding="utf-8") as f:
        f.write(salida)

    print(f"Vista previa escrita: {DST}")
    print(f"Diapositivas: {len(slides)}")
    print(f"Imagenes listas: {len(set(listas))} -> {sorted(set(listas))}")
    print(f"Faltan tomar: {len(pendientes_orden)} -> {pendientes_orden}")
    print(f"Progreso: {pct}%")


if __name__ == "__main__":
    main()
