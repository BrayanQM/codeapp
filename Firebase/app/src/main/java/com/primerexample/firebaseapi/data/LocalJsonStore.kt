package com.primerexample.firebaseapi.data

import android.content.Context
import com.primerexample.firebaseapi.model.Producto
import java.io.File

class LocalJsonStore(context: Context) {

    private val appContext = context.applicationContext
    private val carpeta = File(appContext.filesDir, CARPETA)

    val archivo: File get() = File(carpeta, ARCHIVO)
    val ruta: String get() = archivo.absolutePath

    init {
        preparar()
    }

    private fun preparar() {
        if (!carpeta.exists()) carpeta.mkdirs()
        if (!archivo.exists()) copiarSemilla()
    }

    private fun copiarSemilla() {
        val semilla = runCatching {
            appContext.assets.open("$CARPETA/$ARCHIVO").bufferedReader().use { it.readText() }
        }.getOrNull() ?: "{}"
        archivo.writeText(semilla)
    }

    fun leer(): List<Producto> = runCatching {
        ProductoJson.desdeTexto(archivo.readText())
    }.getOrDefault(emptyList())

    fun escribir(productos: List<Producto>) {
        archivo.writeText(ProductoJson.aTexto(productos))
    }

    companion object {
        const val CARPETA = "database"
        const val ARCHIVO = "productos.json"
    }
}
