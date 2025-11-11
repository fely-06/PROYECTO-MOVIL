package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecetasAdapt(
    private val recetas: MutableList<Receta2>,
    private val onRecetaClick: (Receta2) -> Unit
) : RecyclerView.Adapter<RecetasAdapt.RecetaViewHolder>() {

    inner class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.textViewNombrePlatillo)
        val tvTiempo: TextView = itemView.findViewById(R.id.textViewTiempoPreparacion)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRecetaClick(recetas[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receta, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = recetas[position]
        holder.tvNombre.text = receta.nombre
        holder.tvTiempo.text = "${receta.tiempoPreparacion} min."
    }

    override fun getItemCount() = recetas.size
}