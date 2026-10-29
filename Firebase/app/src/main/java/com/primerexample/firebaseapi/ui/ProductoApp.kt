package com.primerexample.firebaseapi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.primerexample.firebaseapi.data.ProductoRepository
import com.primerexample.firebaseapi.model.Producto
import com.primerexample.firebaseapi.ui.theme.FirebaseApiTheme

@Composable
fun ProductoApp() {
    FirebaseApiTheme {
        val contexto = LocalContext.current
        val alcance = rememberCoroutineScope()
        val controlador = remember { ProductoController(ProductoRepository(contexto), alcance) }

        LaunchedEffect(Unit) { controlador.cargar() }

        when (controlador.pantalla) {
            Pantalla.LISTA -> ListaScreen(controlador)
            Pantalla.FORMULARIO -> key(controlador.editando?.id ?: "nuevo") {
                FormularioScreen(controlador)
            }
        }
    }
}

@Composable
private fun ListaScreen(c: ProductoController) {
    var porBorrar by remember { mutableStateOf<Producto?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { c.abrirNuevo() }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Productos", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            when {
                c.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                c.productos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No hay productos.\nToca + para registrar uno.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(c.productos, key = { it.id }) { producto ->
                        ProductoCard(
                            producto = producto,
                            onEditar = { c.abrirEdicion(producto) },
                            onBorrar = { porBorrar = producto }
                        )
                    }
                }
            }
        }
    }

    porBorrar?.let { producto ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            title = { Text("Eliminar producto") },
            text = { Text("Se eliminara \"${producto.nombre}\" de Firebase y del archivo JSON local.") },
            confirmButton = {
                TextButton(onClick = {
                    c.borrar(producto.id)
                    porBorrar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { porBorrar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProductoCard(
    producto: Producto,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
            if (producto.descripcion.isNotBlank()) {
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producto.precioTexto,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text("Stock: ${producto.stock}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(onClick = onBorrar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun FormularioScreen(c: ProductoController) {
    val original = c.editando ?: Producto()
    val esNuevo = original.id.isBlank()

    var nombre by rememberSaveable { mutableStateOf(original.nombre) }
    var descripcion by rememberSaveable { mutableStateOf(original.descripcion) }
    var precio by rememberSaveable { mutableStateOf(if (original.precio == 0.0) "" else original.precio.toString()) }
    var stock by rememberSaveable { mutableStateOf(if (original.stock == 0) "" else original.stock.toString()) }
    var error by rememberSaveable { mutableStateOf("") }

    fun guardar() {
        if (nombre.isBlank()) {
            error = "El nombre es obligatorio"
            return
        }
        val valorPrecio = precio.replace(",", ".").toDoubleOrNull()
        if (valorPrecio == null || valorPrecio < 0) {
            error = "El precio debe ser un numero mayor o igual a 0"
            return
        }
        val valorStock = stock.toIntOrNull()
        if (valorStock == null || valorStock < 0) {
            error = "El stock debe ser un numero entero mayor o igual a 0"
            return
        }
        c.guardar(
            Producto(
                id = original.id,
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                precio = valorPrecio,
                stock = valorStock
            )
        )
    }

    BackHandler { c.cerrar() }

    Scaffold { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = if (esNuevo) "Nuevo producto" else "Editar producto",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; error = "" },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripcion") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it; error = "" },
                label = { Text("Precio (Bs)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it; error = "" },
                label = { Text("Stock") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { c.cerrar() }, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(onClick = { guardar() }, modifier = Modifier.weight(1f), enabled = !c.ocupado) {
                    Text("Guardar")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
