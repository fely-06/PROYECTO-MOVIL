package com.example.smartmealsproyecto

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.widget.Button

class CrearCuentaDialog() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.crear_cuenta, null)
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
        val botonC = view.findViewById<Button>(R.id.btncancel)
        botonC.setOnClickListener{
            dialog.dismiss()
        }
        return dialog
    }
}