package com.primerexample.firebaseapi.data

import com.primerexample.firebaseapi.model.Producto
import org.json.JSONObject

object ProductoJson {

    fun aTexto(productos: List<Producto>): String = aObjeto(productos).toString(2)

    fun aObjeto(productos: List<Producto>): JSONObject = JSONObject().apply {
        productos.forEach { put(it.id, it.toJson()) }
    }

    fun desdeTexto(texto: String): List<Producto> {
        if (texto.isBlank() || texto.trim() == "null") return emptyList()
        return desdeObjeto(JSONObject(texto))
    }

    fun desdeObjeto(raiz: JSONObject): List<Producto> {
        val lista = mutableListOf<Producto>()
        val claves = raiz.keys()
        while (claves.hasNext()) {
            val id = claves.next()
            val hijo = raiz.optJSONObject(id) ?: continue
            lista += Producto.desdeJson(id, hijo)
        }
        return lista.sortedBy { it.id }
    }
}
