package com.example.smartmealsproyecto

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.AdapterView
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Locale


class AddProductDialog(
    private val existingProducts: MutableList<Producto>,
    private val actualizProd: (MutableList<Producto>) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.add_product, null)
        val autoComplete = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteProducto)
        val cantidadEt = view.findViewById<EditText>(R.id.editTextCantidad)
        val unidadEt = view.findViewById<Spinner>(R.id.spinnerUnidad)
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
        //////////////cargar nombres de recetas////////////////
        var uni: String = ""
        val crud = ClaseCRUD(requireContext())
        crud.iniciarBD()
        val unidades = crud.unidadesMed
        val UnidadesConPlaceholder = listOf("Seleccionar Unidad...") + unidades
        lifecycleScope.launch {

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                UnidadesConPlaceholder
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            unidadEt.adapter = adapter
            unidadEt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        uni = ""
                        return
                    }
                    uni = UnidadesConPlaceholder[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
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
                uni = producto.unidad?.lowercase(Locale.getDefault())?.replace(" ", "") ?: "Seleccionar Unidad..."
                //uni = producto.unidad.lowercase().trim()
                if(uni == "unidade(s)"){
                    uni = "unidad(es)"
                }
                unidadEt.setSelection(UnidadesConPlaceholder.indexOf(uni))
            }
        }

        btnAgregar.setOnClickListener {
            var seagrego: Int = 0
            var semodifico: Boolean = false
            val nombre = autoComplete.text.toString()
            val cantidadStr = cantidadEt.text.toString().trim()
            val Unidad = uni

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
                    lifecycleScope.launch {
                        semodifico = crud.actualizarProducto(existente.Id, existente.unidad, cantidadEt.text.toString().toDouble(), existente.cantidad)
                        if(semodifico == true){
                                crud.consultarInventario(existingProducts)
                                actualizProd(existingProducts)
                        }
                    }

                } else {
                    lifecycleScope.launch {
                        seagrego = crud.insertarProducto(autoComplete.text.toString(), Unidad, cantidadEt.text.toString().toDouble(), "")
                        if(seagrego == 1){
                                crud.consultarInventario(existingProducts)
                            actualizProd(existingProducts)
                        }
                    }
            }
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}