package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DetalleAgendaAdap(private val tipo: String,private val recetas: List<ClassDetAgenda>, private val RecetaClick: (ClassDetAgenda) -> Unit) :
    RecyclerView.Adapter<DetalleAgendaAdap.DetalleAgendaViewHolder>() {

    inner class DetalleAgendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreRec)
        val tvNotas = itemView.findViewById<TextView>(R.id.notas)
        val hora = itemView.findViewById<TextView>(R.id.hora)

        init {
            itemView.setOnClickListener() {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    RecetaClick(recetas[position])
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleAgendaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rec, parent, false)
        return DetalleAgendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetalleAgendaViewHolder, position: Int) {
        val receta = recetas[position]
        holder.tvNombre.text = receta.nombre
        holder.tvNotas.text = receta.notas
        holder.hora.text = receta.hora
    }

    override fun getItemCount() = recetas.size
}