package com.example.smartmealsproyecto

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmealsproyecto.databinding.FragmentRecetasBinding
import kotlinx.coroutines.launch

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
        val crud = ClaseCRUD(requireContext())
        recyclerView = view.findViewById(R.id.recyclerViewProductos)
        fab = view.findViewById(R.id.fabAddProducto)
        crud.iniciarBD()
        lifecycleScope.launch {
            crud.consultarInventario(ProductList)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = ProductoAdap(ProductList, ProductClick = { product -> eliminarProd(product) })
            recyclerView.adapter?.notifyDataSetChanged()
        }
        fab.setOnClickListener {
            showMenu(it)
        }
    }

    private fun eliminarProd(producto: Producto){
        val crud = ClaseCRUD(requireContext())
        var e: Boolean = false
        AlertDialog.Builder(requireContext())
            .setTitle("Advertencia")
            .setMessage("¿Deseas eliminar este producto de tu lista??")
            .setPositiveButton("Eliminar") { _, _ ->

                lifecycleScope.launch {
                    e = crud.eliminarProducto(producto.Id)
                    if(e == true) {
                        crud.consultarInventario(ProductList)
                        recyclerView.adapter?.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            .setNegativeButton("Cancelar", null)
            .show()
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
                    val dialog = AddProductDialog(ProductList,{
                        recyclerView.adapter?.notifyDataSetChanged()
                    })
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