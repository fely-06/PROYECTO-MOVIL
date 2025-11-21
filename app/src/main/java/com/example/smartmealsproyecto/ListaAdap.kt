package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListaAdap(private val productos: MutableList<ClaseCRUD.ItemListaCompra>) :
    RecyclerView.Adapter<ListaAdap.ListaViewHolder>() {

    inner class ListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreProd)
        val tvCantidad = itemView.findViewById<TextView>(R.id.cantidadNecesaria)
        val tvUnidadNec = itemView.findViewById<TextView>(R.id.unidad)
        val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecio)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listacompra, parent, false)
        return ListaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListaViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvNombre.text = producto.nombreIngrediente
        holder.tvCantidad.text = producto.cantidad.toString()
        holder.tvUnidadNec.text = producto.unidad
        holder.tvPrecio.text = "0.0"
    }

    override fun getItemCount() = productos.size
}