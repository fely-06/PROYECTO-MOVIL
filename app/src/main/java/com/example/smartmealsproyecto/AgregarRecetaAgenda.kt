package com.example.smartmealsproyecto

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AgregarRecetaAgenda(
    private val lista: MutableList<ClassDetAgenda>,
    private val fechaDetalle: String,
    private val idReceta: Int,
    private val nombreRec: String,
    private val actualiz: (MutableList<ClassDetAgenda>) -> Unit,
    private val cargarOtraVez: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_agregar_receta_agenda, null)
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
        receta.setText(nombreRec)

        btnAgregar.setOnClickListener {
            var semodifico: Int = 0
            val n = notas.text.toString()
            val hh = timePicker.hour
            val mm = timePicker.minute
            val horaString = String.format("%02d:%02d:00", hh, mm)

            val crud = ClaseCRUD(requireContext())
            crud.iniciarBD()
            lifecycleScope.launch {
                semodifico = crud.insertarRecetaAgenda(idReceta, horaString, n, fechaDetalle, "Merienda")
                if(semodifico == 1){
                    crud.procesarIngredientesReceta(idReceta, fechaDetalle)

                    lista.clear()
                    lista.addAll(crud.consultarDetalleAgendaPorDia(fechaDetalle))
                    actualiz(lista)
                    cargarOtraVez()
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