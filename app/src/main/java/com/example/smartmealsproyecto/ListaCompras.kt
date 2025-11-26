package com.example.smartmealsproyecto

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.smartmealsproyecto.databinding.FragmentListaComprasBottomSheetBinding
import kotlinx.coroutines.launch

class ListaComprasBottomSheetFragment(
    var fechaLim: String,
    var fechaInicio: String,
    var nombreLista: String
) : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private val IngredientesFaltantes = mutableListOf<ClaseCRUD.ItemListaCompra>()
    private var _binding: FragmentListaComprasBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var crud: ClaseCRUD

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaComprasBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crud = ClaseCRUD(requireContext())
        recyclerView = view.findViewById(R.id.recyclerViewCompras)
        crud.iniciarBD()

        lifecycleScope.launch {
            if(fechaLim != "" && fechaInicio != "") {
                // Modo generación de lista
                crud.generarSugerenciasMapeo(fechaInicio, fechaLim)
                val EncabezadoLista = crud.obtenerDatosLita()
                binding.nombrelista.text = EncabezadoLista.nombre
                IngredientesFaltantes.clear()
                IngredientesFaltantes.addAll(crud.obtenerLista())
                binding.btnguardar.isVisible = true
                binding.btncancelar.isVisible = true
                binding.mensaje.isVisible = true
            } else {
                // Modo ver lista existente
                val items = crud.obtenerItemsDeListaPorNombre(ClaseUsuario.iduser, nombreLista)
                binding.nombrelista.text = nombreLista
                IngredientesFaltantes.clear()
                IngredientesFaltantes.addAll(items)
                binding.btnguardar.isVisible = false
                binding.btncancelar.isVisible = false
                binding.mensaje.isVisible = false

                if (items.isEmpty()) {
                    Toast.makeText(requireContext(), "Esta lista está vacía", Toast.LENGTH_SHORT).show()
                }
            }

            // Configurar RecyclerView con el nuevo adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = ListaAdap(
                productos = IngredientesFaltantes,
                unidadesMed = crud.unidadesMed,
                onAgregarInventario = { itemConDatos ->
                    agregarAlInventario(itemConDatos)
                }
            )
            recyclerView.adapter?.notifyDataSetChanged()
        }

        binding.btnguardar.setOnClickListener {
            crud.guardarListaCompraBD(
                lista = ClaseCRUD.ListaCompraTemporal,
            ) { confirmar ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Lista existente")
                    .setMessage("Ya existe una lista con el nombre \"${ClaseCRUD.ListaCompraTemporal.nombre}\".\n¿Deseas reemplazarla?")
                    .setPositiveButton("Reemplazar") { _, _ ->
                        confirmar()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            dismiss()
        }

        binding.btncancelar.setOnClickListener {
            dismiss()
        }
    }

    private fun agregarAlInventario(itemConDatos: ListaAdap.ItemCompraConDatos) {
        lifecycleScope.launch {
            val exitoso = crud.agregarExcedenteInventario(
                nombreIngrediente = itemConDatos.nombreIngrediente,
                cantidadNecesaria = itemConDatos.cantidadNecesaria,
                unidadNecesaria = itemConDatos.unidadNecesaria,
                cantidadComprada = itemConDatos.cantidadComprada,
                unidadComprada = itemConDatos.unidadComprada
            )

            if (exitoso) {
                // Actualizar la UI
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}