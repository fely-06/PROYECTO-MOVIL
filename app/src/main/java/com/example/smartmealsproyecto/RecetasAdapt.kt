package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView

class RecetasAdapt(private val recetas: List<Receta>) :
    RecyclerView.Adapter<RecetasAdapt.RecetaViewHolder>() {
    inner class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre = itemView.findViewById<TextView>(R.id.textViewNombrePlatillo)
        val tvTiempo = itemView.findViewById<TextView>(R.id.textViewTiempoPreparacion)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receta, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = recetas[position]
        holder.tvNombre.text = receta.nombre
        holder.tvTiempo.text = "${receta.tiempoMinutos} min."
    }

    override fun getItemCount() = recetas.size
}