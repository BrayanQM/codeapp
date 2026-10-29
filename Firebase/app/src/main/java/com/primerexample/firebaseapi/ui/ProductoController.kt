package com.primerexample.firebaseapi.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.primerexample.firebaseapi.data.Origen
import com.primerexample.firebaseapi.data.ProductoRepository
import com.primerexample.firebaseapi.data.Resultado
import com.primerexample.firebaseapi.model.Producto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class Pantalla { LISTA, FORMULARIO }

class ProductoController(
    private val repositorio: ProductoRepository,
    private val alcance: CoroutineScope
) {
    var productos by mutableStateOf<List<Producto>>(emptyList()); private set
    var origen by mutableStateOf(Origen.LOCAL); private set
    var mensaje by mutableStateOf("Iniciando..."); private set
    var cargando by mutableStateOf(true); private set
    var ocupado by mutableStateOf(false); private set
    var pantalla by mutableStateOf(Pantalla.LISTA); private set
    var editando by mutableStateOf<Producto?>(null); private set

    val archivoLocal: String get() = repositorio.archivoLocal
    val firebaseConfigurado: Boolean get() = repositorio.firebaseConfigurado
    val urlFirebase: String get() = repositorio.urlFirebase

    fun cargar() = lanzar { mostrar(repositorio.cargar()) }

    fun subir() = lanzar { mostrar(repositorio.subirAFirebase()) }

    fun guardar(producto: Producto) = lanzar {
        val resultado = if (producto.id.isBlank()) {
            repositorio.crear(producto)
        } else {
            repositorio.actualizar(producto)
        }
        mostrar(resultado)
        pantalla = Pantalla.LISTA
    }

    fun borrar(id: String) = lanzar { mostrar(repositorio.eliminar(id)) }

    fun abrirNuevo() {
        editando = Producto()
        pantalla = Pantalla.FORMULARIO
    }

    fun abrirEdicion(producto: Producto) {
        editando = producto
        pantalla = Pantalla.FORMULARIO
    }

    fun cerrar() {
        editando = null
        pantalla = Pantalla.LISTA
    }

    private fun mostrar(resultado: Resultado) {
        productos = resultado.productos
        origen = resultado.origen
        mensaje = resultado.detalle
        cargando = false
    }

    private fun lanzar(bloque: suspend () -> Unit) {
        alcance.launch {
            ocupado = true
            bloque()
            ocupado = false
        }
    }
}
