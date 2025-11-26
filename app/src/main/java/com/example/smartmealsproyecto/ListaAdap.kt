package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class ListaAdap(
    private val productos: MutableList<ClaseCRUD.ItemListaCompra>,
    private val unidadesMed: List<String>,
    private val onAgregarInventario: (ItemCompraConDatos) -> Unit
) : RecyclerView.Adapter<ListaAdap.ListaViewHolder>() {

    data class ItemCompraConDatos(
        val nombreIngrediente: String,
        val cantidadNecesaria: Double,
        val unidadNecesaria: String,
        val cantidadComprada: Double,
        val unidadComprada: String
    )

    inner class ListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxComprado)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProd)
        val tvCantidadNecesaria: TextView = itemView.findViewById(R.id.tvCantidadNecesaria)
        val tvUnidadNecesaria: TextView = itemView.findViewById(R.id.tvUnidadNecesaria)
        val layoutCantidadComprada: LinearLayout = itemView.findViewById(R.id.layoutCantidadComprada)
        val etCantidadComprada: EditText = itemView.findViewById(R.id.etCantidadComprada)
        val spinnerUnidad: Spinner = itemView.findViewById(R.id.spinnerUnidad)
        val btnAgregarInventario: Button = itemView.findViewById(R.id.btnAgregarInventario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listacompra, parent, false)
        return ListaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListaViewHolder, position: Int) {
        val producto = productos[position]

        // Configurar datos básicos
        holder.tvNombre.text = producto.nombreIngrediente
        holder.tvCantidadNecesaria.text = String.format("%.2f", producto.cantidad)
        holder.tvUnidadNecesaria.text = producto.unidad
        holder.checkBox.isChecked = producto.comprado

        // Configurar spinner de unidades
        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            unidadesMed
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerUnidad.adapter = adapter

        // Seleccionar la unidad necesaria por defecto
        val unidadIndex = unidadesMed.indexOfFirst {
            it.equals(producto.unidad, ignoreCase = true)
        }
        if (unidadIndex >= 0) {
            holder.spinnerUnidad.setSelection(unidadIndex)
        }

        // Mostrar/ocultar sección de cantidad comprada
        holder.layoutCantidadComprada.isVisible = producto.comprado


        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            productos[position] = producto.copy(comprado = isChecked)
            holder.layoutCantidadComprada.isVisible = isChecked

            if (isChecked) {
                // Pre-llenar con la cantidad necesaria
                holder.etCantidadComprada.setText(
                    String.format("%.2f", producto.cantidad)
                )
               // moverCompradosAlFinal()
            } else {
                holder.etCantidadComprada.setText("")
            }

        }


        holder.btnAgregarInventario.setOnClickListener {
            val cantidadTexto = holder.etCantidadComprada.text.toString()

            if (cantidadTexto.isBlank()) {
                Toast.makeText(
                    holder.itemView.context,
                    "Ingresa la cantidad comprada",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            try {
                val cantidadComprada = cantidadTexto.toDouble()
                val unidadComprada = holder.spinnerUnidad.selectedItem.toString()

                if (cantidadComprada <= 0) {
                    Toast.makeText(
                        holder.itemView.context,
                        "La cantidad debe ser mayor a 0",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val itemConDatos = ItemCompraConDatos(
                    nombreIngrediente = producto.nombreIngrediente,
                    cantidadNecesaria = producto.cantidad,
                    unidadNecesaria = producto.unidad,
                    cantidadComprada = cantidadComprada,
                    unidadComprada = unidadComprada
                )

                onAgregarInventario(itemConDatos)

                // Limpiar campos después de agregar
                holder.etCantidadComprada.setText("")
                holder.checkBox.isChecked = false

            } catch (e: NumberFormatException) {
                Toast.makeText(
                    holder.itemView.context,
                    "Cantidad inválida",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount() = productos.size
    fun moverCompradosAlFinal() {
        productos.sortBy { it.comprado } // false (0) va antes que true (1)
        notifyDataSetChanged()
    }
}