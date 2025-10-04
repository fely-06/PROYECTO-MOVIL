package com.example.smartmealsproyecto

import android.content.Intent
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
import com.example.smartmealsproyecto.databinding.FragmentRecetasBinding

class Inventario : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: ImageButton
    private val ProductList = mutableListOf<Producto>()
    private var nextId = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewProductos)
        fab = view.findViewById(R.id.fabAddProducto)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        ProductList.apply { add(Producto(nextId++,"Manzana", 10, "Piezas"))
            add(Producto(nextId++,"Banana", 5, "Piezas"))
            add(Producto(nextId++,"Naranja", 8, "Piezas"))
            add(Producto(nextId++,"Fresa", 12, "Piezas"))
            add(Producto(nextId++,"Kiwi", 3 , "Piezas"))}


        recyclerView.adapter = ProductoAdap(ProductList)

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

                }
                "Agregar Manualmente" -> {
                    val dialog = AddProductDialog(ProductList) { producto ->
                        val index = ProductList.indexOfFirst { it.Id == producto.Id }
                        if (index != -1) {
                            ProductList[index] = producto
                        } else {
                            ProductList.add(producto)
                            if (producto.Id >= nextId) {
                                nextId = producto.Id + 1
                            }
                        }
                        recyclerView.adapter?.notifyDataSetChanged()
                        Toast.makeText(context, "Producto guardado", Toast.LENGTH_SHORT).show()
                    }
                    dialog.show(childFragmentManager, "AddProductDialog")
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