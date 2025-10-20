package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecetasGAdap(
    private val recetas: List<Receta2>,
    private val onRecetaClick: (Receta2) -> Unit,
    private val onCheckBoxCheck: (Receta2) -> Unit
) : RecyclerView.Adapter<RecetasGAdap.RecetaViewHolder>() {

    inner class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.textViewNombrePlatillo)
        val tvTiempo: TextView = itemView.findViewById(R.id.textViewTiempoPreparacion)
        val checkB: CheckBox = itemView.findViewById(R.id.CheckReceta)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRecetaClick(recetas[position])
                }
            }
            checkB.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val receta = recetas[position]
                    receta.favorita == isChecked
                    onCheckBoxCheck(receta)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recetaglobal, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = recetas[position]
        holder.tvNombre.text = receta.nombre
        holder.tvTiempo.text = "${receta.tiempoPreparacion} min."
        holder.checkB.isChecked = receta.favorita
    }
    override fun getItemCount() = recetas.size
}