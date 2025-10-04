package com.example.smartmealsproyecto

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.AdapterView

class AddProductDialog(
    private val existingProducts: List<Producto>,
    private val onProductAdded: (Producto) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.add_product, null)
        //val nombreEt = view.findViewById<EditText>(R.id.editTextNombre)
        val autoComplete = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteProducto)
        val cantidadEt = view.findViewById<EditText>(R.id.editTextCantidad)
        val unidadEt = view.findViewById<EditText>(R.id.editTextUnidad)
        val btnAgregar = view.findViewById<Button>(R.id.btnguardar)
        val btnCancelar = view.findViewById<Button>(R.id.btncancelar)
        val dialog = Dialog(requireContext())
        dialog.setContentView(view)

        val window = dialog.window
        if (window != null) {
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
        //obtener productos exist
        val nombres = existingProducts.map { it.nombre }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            nombres
        )
        autoComplete.setAdapter(adapter)

        autoComplete.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val nombreSeleccionado = adapter.getItem(position) ?: return@OnItemClickListener
            val producto = existingProducts.find { it.nombre == nombreSeleccionado }
            if (producto != null) {
                unidadEt.setText(producto.unidad)
            }
        }

        btnAgregar.setOnClickListener {

            val nombre = autoComplete.text.toString().trim()
            val cantidadStr = cantidadEt.text.toString().trim()
            val Unidad = unidadEt.text.toString().trim()

            if (nombre.isEmpty() || cantidadStr.isEmpty()) {
                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad = cantidadStr.toIntOrNull()
            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(context, "Cantidad inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existente = existingProducts.find { it.nombre.equals(nombre, ignoreCase = true) }
                if (existente != null) {

                    val prod = Producto(
                        existente.Id,
                        existente.nombre,
                        existente.cantidad + cantidad,
                        Unidad
                    )
                    onProductAdded(prod)

                } else {
                val nuevoId = (existingProducts.maxByOrNull { it.Id }?.Id ?: 0) + 1
                val prod = Producto(nuevoId, nombre, cantidad, Unidad)
                    onProductAdded(prod)
            }
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}