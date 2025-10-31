package com.example.smartmealsproyecto

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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
        val botonCrear = view.findViewById<Button>(R.id.btncrear)
        val user = view.findViewById<EditText>(R.id.Usuario)
        val password = view.findViewById<EditText>(R.id.passw)
        botonC.setOnClickListener{
            dialog.dismiss()
        }
        botonCrear.setOnClickListener {
            if(user.text.toString().contains(" ")){
                Toast.makeText(context, "Nombre de usuario y contraseña no validos", Toast.LENGTH_LONG).show()
            }
            else if(user.text.toString().trim().isNotBlank() && password.text.toString().trim().isNotBlank()){
                val crud = ClaseCRUD(requireContext())
                crud.iniciarBD()
                lifecycleScope.launch {
                var v: Int = crud.insertarUsuario(user.text.toString(),password.text.toString())
                if(v==1){
                    user.setText("")
                    password.setText("")
                    dialog.dismiss()
                }
                }
            }
            else{
                Toast.makeText(context, "Favor de llenar todos los campos", Toast.LENGTH_LONG).show()
            }
        }
        return dialog
    }
}