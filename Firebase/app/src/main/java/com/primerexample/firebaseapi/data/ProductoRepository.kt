package com.primerexample.firebaseapi.data

import android.content.Context
import com.primerexample.firebaseapi.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Origen { FIREBASE, LOCAL }

data class Resultado(
    val productos: List<Producto>,
    val origen: Origen,
    val detalle: String
)

class ProductoRepository(context: Context) {

    private val local = LocalJsonStore(context)
    private val api = FirebaseApi(
        com.primerexample.firebaseapi.BuildConfig.FIREBASE_DB_URL,
        com.primerexample.firebaseapi.BuildConfig.FIREBASE_DB_TOKEN
    )

    val archivoLocal: String get() = local.ruta
    val firebaseConfigurado: Boolean get() = api.configurada
    val urlFirebase: String get() = api.url
    val motivoSinConfigurar: String get() = api.motivoSinConfigurar

    suspend fun cargar(): Resultado = conHilo {
        try {
            val productos = api.listar()
            if (productos.isEmpty()) {
                // Firebase responde bien pero no hay nada guardado: no se pisa el
                // JSON local, se avisa y se deja subir con "Subir JSON a Firebase".
                return@conHilo Resultado(
                    local.leer(),
                    Origen.LOCAL,
                    "Firebase esta vacio. Usa 'Subir JSON a Firebase' para sembrarlo"
                )
            }
            local.escribir(productos)
            Resultado(productos, Origen.FIREBASE, "Traido de Firebase: ${productos.size} productos")
        } catch (e: Exception) {
            val productos = local.leer()
            Resultado(productos, Origen.LOCAL, "Solo JSON local (${productos.size}): ${e.message}")
        }
    }

    suspend fun subirAFirebase(): Resultado = conHilo {
        val locales = local.leer()
        if (locales.isEmpty()) {
            return@conHilo Resultado(locales, Origen.LOCAL, "El JSON local esta vacio, no hay nada que subir")
        }
        try {
            locales.forEach { api.subir(it) }
            Resultado(locales, Origen.FIREBASE, "Subidos ${locales.size} productos del JSON a Firebase")
        } catch (e: Exception) {
            Resultado(locales, Origen.LOCAL, "No se pudo subir a Firebase: ${e.message}")
        }
    }

    suspend fun crear(producto: Producto): Resultado = conHilo {
        val idLocal = "-L${System.currentTimeMillis()}"
        local.escribir(local.leer() + producto.copy(id = idLocal))
        try {
            val guardado = api.crear(producto)
            val productos = local.leer().map { if (it.id == idLocal) guardado else it }
            local.escribir(productos)
            Resultado(productos, Origen.FIREBASE, "Guardado en Firebase y en database/productos.json")
        } catch (e: Exception) {
            Resultado(local.leer(), Origen.LOCAL, "Guardado solo en database/productos.json: ${e.message}")
        }
    }

    suspend fun actualizar(producto: Producto): Resultado = conHilo {
        val productos = local.leer().map { if (it.id == producto.id) producto else it }
        local.escribir(productos)
        try {
            api.actualizar(producto)
            Resultado(productos, Origen.FIREBASE, "Actualizado en Firebase y en database/productos.json")
        } catch (e: Exception) {
            Resultado(productos, Origen.LOCAL, "Actualizado solo en database/productos.json: ${e.message}")
        }
    }

    suspend fun eliminar(id: String): Resultado = conHilo {
        val productos = local.leer().filterNot { it.id == id }
        local.escribir(productos)
        try {
            api.eliminar(id)
            Resultado(productos, Origen.FIREBASE, "Eliminado de Firebase y de database/productos.json")
        } catch (e: Exception) {
            Resultado(productos, Origen.LOCAL, "Eliminado solo de database/productos.json: ${e.message}")
        }
    }

    private suspend fun <T> conHilo(bloque: () -> T): T = withContext(Dispatchers.IO) { bloque() }
}
