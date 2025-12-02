package com.example.smartmealsproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // 🆕 Agregar esta importación

class RecetasGAdap(
    private val recetas: List<Receta2>,
    private val onRecetaClick: (Receta2) -> Unit,
    private val onCheckBoxCheck: (Receta2, Int) -> Unit
) : RecyclerView.Adapter<RecetasGAdap.RecetaViewHolder>() {

    inner class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.textViewNombrePlatillo)
        val tvTiempo: TextView = itemView.findViewById(R.id.textViewTiempoPreparacion)
        val checkB: CheckBox = itemView.findViewById(R.id.CheckReceta)
        val imageViewPlatillo: ImageView = itemView.findViewById(R.id.imageViewPlatillo) // 🆕

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
                    if(receta.favorita == isChecked){
                        onCheckBoxCheck(receta,1)
                    }
                    else{
                        onCheckBoxCheck(receta,0)
                    }
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
        if (!receta.imagenRuta.isNullOrEmpty()) {
            holder.imageViewPlatillo.load(receta.imagenRuta) {
                crossfade(true)
                placeholder(R.drawable.comida)
                error(R.drawable.comida)
            }
        } else {
            holder.imageViewPlatillo.setImageResource(R.drawable.comida)
        }
    }

    override fun getItemCount() = recetas.size
}