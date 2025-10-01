package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecetasAdapt(private val recetas: List<Receta>) :
    RecyclerView.Adapter<RecetasAdapt.RecetaViewHolder>() {

    class RecetaViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val textView = TextView(parent.context).apply {
            textSize = 20f
            setPadding(16, 16, 16, 16)
        }
        return RecetaViewHolder(textView)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.textView.text = recetas[position].nombre
    }

    override fun getItemCount() = recetas.size
}