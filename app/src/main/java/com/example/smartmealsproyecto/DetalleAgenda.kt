package com.example.smartmealsproyecto

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmealsproyecto.databinding.FragmentDetalleAgendaBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.smartmealsproyecto.databinding.FragmentListaComprasBottomSheetBinding
import kotlinx.coroutines.launch

class DetalleAgenda(private var fechaSelec: String,private val cargarOtraVez: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: FragmentDetalleAgendaBinding? = null
    private val binding get() = _binding!!
    private val RecetasList = mutableListOf<ClassDetAgenda>()
    private val recetaslistComb = mutableListOf<Receta2>()

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
            binding.recyclerViewRecetasD.adapter = DetalleAgendaAdap("Desayuno",RecetasList, { receta -> eliminarRec(receta) },{ receta -> actualizarRec(receta)})
        }
        //////////////cargar nombres de recetas////////////////
        var nombre: String = ""
        var idR: Int = 0
        lifecycleScope.launch {
            val recetas = crud.obtenerMisRecetas(recetaslistComb, recetaslistComb)
            val nomRe = recetaslistComb.map { it.nombre }
            val idReceta = recetaslistComb.map { it.id }
            val recetasConPlaceholder = listOf("Seleccionar Receta...") + nomRe
            var idConPlaceholder: List<Int> = listOf(0) + idReceta
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                recetasConPlaceholder
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerRecetas.adapter = adapter
            binding.spinnerRecetas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        nombre = ""
                        return
                    }
                    nombre = recetasConPlaceholder[position]
                    idR = idConPlaceholder[position]

                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
        binding.agregaRec.setOnClickListener {
            if(nombre == ""){
                Toast.makeText(context, "Selecciona Primero una Receta", Toast.LENGTH_SHORT).show()
            }
            else {
                val dialog = AgregarRecetaAgenda(RecetasList, fechaSelec, idR, nombre, {
                    binding.recyclerViewRecetasD.adapter?.notifyDataSetChanged()
                },{
                    cargarOtraVez()
                })
                dialog.show(childFragmentManager, "AddProductDialog")
                binding.spinnerRecetas.setSelection(0)
            }
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

    private fun actualizarRec(receta: ClassDetAgenda){
        val dialog = ActualizarDetalleAgendaDialog(receta,RecetasList,fechaSelec,{
            binding.recyclerViewRecetasD.adapter?.notifyDataSetChanged()
        })
        dialog.show(childFragmentManager, "AddProductDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}