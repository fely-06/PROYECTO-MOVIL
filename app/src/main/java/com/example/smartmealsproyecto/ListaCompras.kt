package com.example.smartmealsproyecto

import android.app.AlertDialog
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.smartmealsproyecto.databinding.FragmentListaComprasBottomSheetBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListaComprasBottomSheetFragment(var fechaLim: String, var fechaInicio: String) : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private val IngredientesFaltantes = mutableListOf<ClaseCRUD.ItemListaCompra>()
    private var _binding: FragmentListaComprasBottomSheetBinding? = null
    private val binding get() = _binding!!

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
        val crud = ClaseCRUD(requireContext())
        recyclerView = view.findViewById(R.id.recyclerViewCompras)
        crud.iniciarBD()
        lifecycleScope.launch {
            //val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            crud.generarSugerenciasMapeo(fechaInicio, fechaLim)
            val EncabezadoLista = crud.obtenerDatosLita()
            binding.nombrelista.text = EncabezadoLista.nombre
            IngredientesFaltantes.clear()
            IngredientesFaltantes.addAll(crud.obtenerLista())
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = ListaAdap(IngredientesFaltantes)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}