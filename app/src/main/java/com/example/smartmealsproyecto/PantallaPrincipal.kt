package com.example.smartmealsproyecto

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.smartmealsproyecto.databinding.ActivityMainBinding
import com.example.smartmealsproyecto.databinding.ActivityPantallaPrincipalBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class PantallaPrincipal : AppCompatActivity() {
    lateinit var binding: ActivityPantallaPrincipalBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_principal)
        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        fragmentos(Home())
        binding.bottomNavigation.setOnItemSelectedListener{
            when(it.itemId){
                R.id.homeFragment->fragmentos(Home())
                R.id.saveFragment->fragmentos(Recetas())
                R.id.makeFragment->fragmentos(Calendario())
                R.id.InvFragment->fragmentos(Inventario())
                R.id.profileFragment->fragmentos(Perfil())
            }
            true
        }

    }
    fun fragmentos(fragment: Fragment){
        var fragment_manager=supportFragmentManager
        var transaction=fragment_manager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment).commit()
    }
}