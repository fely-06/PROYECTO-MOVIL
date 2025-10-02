package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IngredientesAdapter(
    private val ingredientes: List<Ingrediente>,
    private val onEliminarClick: (Ingrediente) -> Unit
) : RecyclerView.Adapter<IngredientesAdapter.IngredienteViewHolder>() {

    inner class IngredienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIngrediente: TextView = itemView.findViewById(R.id.textViewIngrediente)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.buttonEliminarIngrediente)

        fun bind(ingrediente: Ingrediente) {
            tvIngrediente.text = ingrediente.toString()
            btnEliminar.setOnClickListener {
                onEliminarClick(ingrediente)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingrediente, parent, false)
        return IngredienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredienteViewHolder, position: Int) {
        holder.bind(ingredientes[position])
    }

    override fun getItemCount() = ingredientes.size
}