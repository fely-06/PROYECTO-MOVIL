package com.example.smartmealsproyecto

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmealsproyecto.databinding.FragmentDetalleAgendaBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.smartmealsproyecto.databinding.FragmentListaComprasBottomSheetBinding
import kotlinx.coroutines.launch

class DetalleAgenda(private var fechaSelec: String) : BottomSheetDialogFragment() {

    private var _binding: FragmentDetalleAgendaBinding? = null
    private val binding get() = _binding!!
    private val RecetasList = mutableListOf<ClassDetAgenda>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleAgendaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val crud = ClaseCRUD(requireContext())
        crud.iniciarBD()
        binding.fechadia.text = fechaSelec
        lifecycleScope.launch {
            RecetasList.clear()
            RecetasList.addAll(crud.consultarDetalleAgendaPorDia(fechaSelec))
            binding.recyclerViewRecetasD.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewRecetasD.adapter = DetalleAgendaAdap("Desayuno",RecetasList) { receta -> eliminarRec(receta) }
        }
        binding.agregaRec.setOnClickListener {

        }
    }
    private fun eliminarRec(receta: ClassDetAgenda){
        val crud = ClaseCRUD(requireContext())
        var e: Boolean = false
        AlertDialog.Builder(requireContext())
            .setTitle("Advertencia")
            .setMessage("¿Deseas eliminar esta receta de tu lista??")
            .setPositiveButton("Eliminar") { _, _ ->

                lifecycleScope.launch {
                    e = crud.eliminarRecetaDeAgenda(receta.id)
                    if(e == true) {
                        RecetasList.clear()
                        RecetasList.addAll(crud.consultarDetalleAgendaPorDia(fechaSelec))
                        binding.recyclerViewRecetasD.adapter?.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Receta eliminada", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}