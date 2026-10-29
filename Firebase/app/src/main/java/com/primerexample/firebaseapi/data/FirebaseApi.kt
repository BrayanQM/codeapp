package com.primerexample.firebaseapi.data

import com.primerexample.firebaseapi.model.Producto
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class FirebaseApi(private val urlBase: String, private val token: String = "") {

    val url: String get() = urlBase

    val configurada: Boolean
        get() = urlBase.startsWith("https://") &&
            !urlBase.contains("PLACEHOLDER") &&
            !urlBase.contains("TU-PROYECTO") &&
            !urlBase.contains("PEGAR_AQUI")

    val motivoSinConfigurar: String
        get() = when {
            urlBase.isBlank() -> "Falta firebase.db.url en local.properties"
            !urlBase.startsWith("https://") -> "firebase.db.url debe empezar por https://"
            else -> "firebase.db.url sigue siendo el marcador de posicion, pon la URL real"
        }

    fun listar(): List<Producto> {
        val texto = peticion("GET", rutaRaiz(), null)
        if (texto.isBlank() || texto.trim() == "null") return emptyList()
        return ProductoJson.desdeObjeto(JSONObject(texto))
    }

    fun crear(producto: Producto): Producto {
        val texto = peticion("POST", rutaRaiz(), producto.toJson().toString())
        val id = JSONObject(texto).optString("name")
        return producto.copy(id = id)
    }

    fun actualizar(producto: Producto) {
        peticion("PATCH", ruta(producto.id), producto.toJson().toString())
    }

    fun subir(producto: Producto) {
        peticion("PUT", ruta(producto.id), producto.toJson().toString())
    }

    fun eliminar(id: String) {
        peticion("DELETE", ruta(id), null)
    }

    private fun rutaRaiz() = "$urlBase/$COLECCION.json$autenticacion"

    private fun ruta(id: String) = "$urlBase/$COLECCION/$id.json$autenticacion"

    private val autenticacion: String
        get() = if (token.isBlank()) "" else "?auth=$token"

    private fun peticion(metodo: String, url: String, cuerpo: String?): String {
        if (!configurada) {
            throw IllegalStateException(motivoSinConfigurar)
        }
        val conexion = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = metodo
            connectTimeout = TIEMPO_LIMITE_MS
            readTimeout = TIEMPO_LIMITE_MS
            setRequestProperty("Accept", "application/json")
            if (cuerpo != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (cuerpo != null) {
                conexion.outputStream.use { it.write(cuerpo.toByteArray(Charsets.UTF_8)) }
            }
            val codigo = conexion.responseCode
            if (codigo !in 200..299) {
                val detalle = conexion.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException(explicar(codigo, detalle))
            }
            return conexion.inputStream.bufferedReader().use { it.readText() }
        } catch (e: java.net.UnknownHostException) {
            throw IllegalStateException("Sin conexion a Internet", e)
        } catch (e: java.net.SocketTimeoutException) {
            throw IllegalStateException("Firebase no respondio a tiempo", e)
        } finally {
            conexion.disconnect()
        }
    }

    private fun explicar(codigo: Int, detalle: String): String = when (codigo) {
        401, 403 ->
            "Firebase rechazo la peticion (HTTP $codigo). Las reglas de seguridad bloquean el acceso: " +
                "en la consola abre la base de datos y ponla en Modo de prueba."
        404 ->
            "La base de datos no existe todavia (HTTP 404). Creala en la consola de Firebase: " +
                "Build > Realtime Database > Crear base de datos. URL usada: $urlBase"
        else -> "Firebase respondio HTTP $codigo: ${detalle.take(140)}"
    }

    companion object {
        private const val COLECCION = "productos"
        private const val TIEMPO_LIMITE_MS = 10_000
    }
}
