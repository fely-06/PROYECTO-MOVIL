package com.example.smartmealsproyecto

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.AdapterView
import android.widget.TimePicker
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch


class ActualizarDetalleAgendaDialog(
    private val DetalleAgenda: ClassDetAgenda,
    private val lista: MutableList<ClassDetAgenda>,
    private val fechaDetalle: String,
    private val actualiz: (MutableList<ClassDetAgenda>) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.update_detalleagenda, null)
        val receta = view.findViewById<EditText>(R.id.recetanombre)
        val notas = view.findViewById<EditText>(R.id.notas)
        val horaPic = view.findViewById<TimePicker>(R.id.timePic)
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
        val timePicker = horaPic
        timePicker.setIs24HourView(true)
        receta.setText(DetalleAgenda.nombre)
        notas.setText(DetalleAgenda.notas)
        val partes = DetalleAgenda.hora.split(":")
        if (partes.size >= 2) {
            val hora = partes[0].toInt()
            val minuto = partes[1].toInt()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                horaPic.hour = hora
                horaPic.minute = minuto
            } else {
                horaPic.currentHour = hora
                horaPic.currentMinute = minuto
            }
        }
        btnAgregar.setOnClickListener {
            var semodifico: Boolean = false
            val nombre = receta.text.toString()
            val n = notas.text.toString()
            val hh = timePicker.hour
            val mm = timePicker.minute
            val horaString = String.format("%02d:%02d:00", hh, mm)

            val crud = ClaseCRUD(requireContext())
            crud.iniciarBD()
                lifecycleScope.launch {
                    semodifico = crud.actualizarDetalleAgenda(DetalleAgenda.id, horaString, n)
                    if(semodifico == true){
                        lista.clear()
                        lista.addAll(crud.consultarDetalleAgendaPorDia(fechaDetalle))
                        actualiz(lista)
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