#!/usr/bin/env bash
# Verifica los 5 endpoints CRUD contra Firebase Realtime Database.
# Lee la URL (y el token opcional) de local.properties, misma fuente que la app.
set -u

PROPS="$(dirname "$0")/local.properties"
URL=$(grep -E '^firebase\.db\.url=' "$PROPS" | cut -d= -f2- | tr -d ' \r')
TOKEN=$(grep -E '^firebase\.db\.token=' "$PROPS" | cut -d= -f2- | tr -d ' \r')
AUTH=""
[[ -n "$TOKEN" ]] && AUTH="?auth=$TOKEN"
BASE="$URL/productos.json$AUTH"

if [[ -z "$URL" || "$URL" == *PEGAR_AQUI* ]]; then
  echo "Falta configurar firebase.db.url en local.properties"
  exit 1
fi

ok()   { printf '  \033[32mOK\033[0m   %s\n' "$1"; }
fail() { printf '  \033[31mFALLA\033[0m %s\n' "$1"; }
check(){ # check <etiqueta> <codigo_esperado> <codigo_obtenido> <cuerpo>
  if [[ "$2" == "$3" ]]; then ok "$1 (HTTP $3)"; else fail "$1 -> HTTP $3: ${4:0:160}"; fi
}

echo "Base: $URL"
echo

if [[ "$TOKEN" == "" ]]; then
  echo "AVISO: la BD esta en modo prueba (sin token). Si las reglas exigen"
  echo "autenticacion, cambia el modo a 'Bloqueado' con reglas o usa un token."
  echo
fi

echo "1) GET  /productos.json        (listar)"
c=$(curl -s -o /tmp/fb_r -w '%{http_code}' --max-time 15 "$BASE"); check "listar" 200 "$c" "$(cat /tmp/fb_r)"
if [[ "$c" == "404" ]]; then
  echo
  echo "La base de datos NO existe todavia. Creala en:"
  echo "  https://console.firebase.google.com/project/classfirebase-d856a/database"
  echo "Boton 'Crear base de datos' > ubicacion > 'Modo de prueba' > Crear"
  echo "Si la creaste en otra region, corrige firebase.db.url en local.properties."
  exit 1
fi
if [[ "$c" == "401" || "$c" == "403" ]]; then
  echo
  echo "Las reglas de seguridad bloquean el acceso. Pon la BD en 'Modo de prueba'"
  echo "o escribe reglas:  { \"rules\": { \".read\": true, \".write\": true } }"
  exit 1
fi

echo "2) POST /productos.json        (crear)"
c=$(curl -s -o /tmp/fb_r -w '%{http_code}' --max-time 15 -X POST \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Producto de prueba","descripcion":"verificacion","precio":9.99,"stock":1}' \
  "$BASE")
check "crear" 200 "$c" "$(cat /tmp/fb_r)"
ID=$(python3 -c "import json,sys;print(json.load(open('/tmp/fb_r')).get('name',''))" 2>/dev/null)
echo "     id generado: ${ID:-<ninguno>}"

if [[ -n "$ID" ]]; then
  echo "3) GET  /productos/$ID.json  (ver uno)"
  c=$(curl -s -o /tmp/fb_r -w '%{http_code}' --max-time 15 "$URL/productos/$ID.json$AUTH")
  check "ver uno" 200 "$c" "$(cat /tmp/fb_r)"

  echo "4) PATCH /productos/$ID.json (actualizar)"
  c=$(curl -s -o /tmp/fb_r -w '%{http_code}' --max-time 15 -X PATCH \
    -H 'Content-Type: application/json' -d '{"precio":19.99}' \
    "$URL/productos/$ID.json$AUTH")
  check "actualizar" 200 "$c" "$(cat /tmp/fb_r)"

  echo "5) DELETE /productos/$ID.json (eliminar)"
  c=$(curl -s -o /tmp/fb_r -w '%{http_code}' --max-time 15 -X DELETE "$URL/productos/$ID.json$AUTH")
  check "eliminar" 200 "$c" "$(cat /tmp/fb_r)"
fi

echo
echo "Contenido final:"
curl -s --max-time 15 "$BASE" | python3 -m json.tool 2>/dev/null | head -30
