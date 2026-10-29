package com.primerexample.firebaseapi.model

import org.json.JSONObject
import java.util.Locale

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("nombre", nombre)
        put("descripcion", descripcion)
        put("precio", precio)
        put("stock", stock)
    }

    val precioTexto: String
        get() = MONEDA + String.format(Locale.US, "%.2f", precio)

    companion object {
        const val MONEDA = "Bs "

        fun desdeJson(id: String, json: JSONObject) = Producto(
            id = id,
            nombre = json.optString("nombre"),
            descripcion = json.optString("descripcion"),
            precio = json.optDouble("precio", 0.0),
            stock = json.optInt("stock", 0)
        )
    }
}
