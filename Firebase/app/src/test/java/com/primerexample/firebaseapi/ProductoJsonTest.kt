package com.primerexample.firebaseapi

import com.primerexample.firebaseapi.data.ProductoJson
import com.primerexample.firebaseapi.model.Producto
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductoJsonTest {

    private val semilla = """
        {
          "-Nseed001": {
            "nombre": "Teclado mecanico",
            "descripcion": "Teclado RGB",
            "precio": 89.9,
            "stock": 12
          }
        }
    """.trimIndent()

    @Test
    fun `lee el archivo json con clave como id`() {
        val productos = ProductoJson.desdeTexto(semilla)

        assertEquals(1, productos.size)
        assertEquals("-Nseed001", productos.first().id)
        assertEquals("Teclado mecanico", productos.first().nombre)
        assertEquals(89.9, productos.first().precio, 0.001)
        assertEquals(12, productos.first().stock)
    }

    @Test
    fun `el formato escrito tiene la misma forma que el de firebase`() {
        val productos = ProductoJson.desdeTexto(semilla)
        val objeto = ProductoJson.aObjeto(productos)

        assertTrue(objeto.has("-Nseed001"))
        assertEquals("Teclado RGB", objeto.getJSONObject("-Nseed001").getString("descripcion"))
    }

    @Test
    fun `ida y vuelta conserva los datos`() {
        val original = Producto("-Nabc", "Mouse", "Inalambrico", 34.5, 25)

        val recuperada = ProductoJson.desdeTexto(ProductoJson.aTexto(listOf(original)))

        assertEquals(original, recuperada.single())
    }

    @Test
    fun `el cuerpo json no incluye el id porque es la clave`() {
        val cuerpo = Producto("-Nabc", "Mouse", "Inalambrico", 34.5, 25).toJson()

        assertEquals(setOf("nombre", "descripcion", "precio", "stock"), cuerpo.keys().asSequence().toSet())
    }

    @Test
    fun `un objeto vacio o nulo produce una lista vacia`() {
        assertTrue(ProductoJson.desdeTexto("").isEmpty())
        assertTrue(ProductoJson.desdeTexto("null").isEmpty())
        assertTrue(ProductoJson.desdeTexto("{}").isEmpty())
    }

    @Test
    fun `los campos ausentes usan valores por defecto`() {
        val productos = ProductoJson.desdeObjeto(JSONObject("""{"-Nx": {"nombre": "Sin datos"}}"""))

        assertEquals("Sin datos", productos.single().nombre)
        assertEquals("", productos.single().descripcion)
        assertEquals(0.0, productos.single().precio, 0.001)
        assertEquals(0, productos.single().stock)
    }

    @Test
    fun `precio y stock se muestran con dos decimales`() {
        assertEquals("Bs 34.50", Producto("1", "Mouse", "", 34.5, 1).precioTexto)
        assertEquals("Bs 0.00", Producto("1", "Gratis", "", 0.0, 1).precioTexto)
    }
}
