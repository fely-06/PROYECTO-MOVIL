package com.example.smartmealsproyecto

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.smartmealsproyecto.databinding.ActivityMainBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val BotonLogin = findViewById<Button>(R.id.BotonLogin)
        val BotonCrear = findViewById<Button>(R.id.BotonCrear)
        val user = findViewById<EditText>(R.id.usuario)
        val password = findViewById<EditText>(R.id.passw)

        BotonLogin.setOnClickListener {
            var v: Int =0
            if(user.text.toString().trim().isNotBlank() && password.text.toString().trim().isNotBlank()){
                val crud = ClaseCRUD(this)
                crud.iniciarBD()
                lifecycleScope.launch {
                    v = crud.insertarUsuario(user.text.toString(),password.text.toString())
                }
            }
            else{
                Toast.makeText(this, "Favor de llenar todos los campos", Toast.LENGTH_LONG).show()
            }
            if(v ==1){
                val intent = Intent(this, PantallaPrincipal::class.java)
                startActivity(intent)
            }
            else{
                user.setText("")
                password.setText("")
            }
        }
        BotonCrear.setOnClickListener {
            val dialog = CrearCuentaDialog()
            dialog.show(supportFragmentManager, "CrearCuentaDialog")
        }


    }


}