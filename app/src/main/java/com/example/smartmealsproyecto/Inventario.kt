package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Inventario : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa vistas
        recyclerView = view.findViewById(R.id.recyclerViewProductos)
        fab = view.findViewById(R.id.fabAddProducto)

        // Configura RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val productos = listOf(
            Producto("Manzana", 10),
            Producto("Banana", 5),
            Producto("Naranja", 8),
            Producto("Fresa", 12),
            Producto("Kiwi", 3)
        )

        recyclerView.adapter = ProductoAdap(productos)

        // Configura FAB
        fab.setOnClickListener {
            showMenu(it)
        }
    }

    private fun showMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add("Escanear")
        popup.menu.add("Agregar Manualmente")

        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Escanear" -> {
                    Toast.makeText(requireContext(), "Escaneando...", Toast.LENGTH_SHORT).show()
                }
                "Agregar Manualmente" -> {
                    Toast.makeText(requireContext(), "Abriendo formulario...", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}