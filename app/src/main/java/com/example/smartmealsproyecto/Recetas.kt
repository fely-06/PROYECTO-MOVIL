package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmealsproyecto.databinding.FragmentRecetasBinding

class Recetas : Fragment() {
    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: RecetasAdapt

    companion object {
        val recetasList = mutableListOf<Receta2>()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        cargarmisRecetas()
        setupSearch()
        setupSortButtons()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = RecetasAdapt(recetasList) { receta ->
            abrirDetalleReceta(receta)
        }
        binding.rec.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@Recetas.adapter
        }
    }
    private fun cargarmisRecetas() {
        recetasList.clear()
        recetasList.addAll(RecetasTotales.misRecetas)
        adapter.notifyDataSetChanged()
    }
    private fun setupSearch() {
        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editTextSearch.text.toString().trim()
                filterRecetas(query)
                true
            } else {
                false
            }
        }
    }

    private fun setupSortButtons() {
        binding.iconSortAZ.setOnClickListener {
            recetasList.sortBy { it.nombre }
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Ordenado A-Z", Toast.LENGTH_SHORT).show()
        }

        binding.iconSortZA.setOnClickListener {
            recetasList.sortByDescending { it.nombre }
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Ordenado Z-A", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterRecetas(query: String) {
        val filtered = if (query.isEmpty()) {
            recetasList
        } else {
            recetasList.filter {
                it.nombre.contains(query, ignoreCase = true)
            }
        }
        recetasList.clear()
        recetasList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val fragment = NuevaRecetaFragment.newInstance()
            fragment.setOnRecetaGuardadaListener { receta ->

                RecetasTotales.todasLasRecetas.add(receta)

                Toast.makeText(requireContext(), "Receta guardada", Toast.LENGTH_SHORT).show()
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun abrirDetalleReceta(receta: Receta2) {
        val detalleFragment = DetalleRecetaFragment.newInstance(receta.id, false)
        detalleFragment.setOnRecetaActualizadaListener {
            adapter.notifyDataSetChanged()
        }
        detalleFragment.setOnRecetaEliminadaListener { recetaId ->
            recetasList.removeAll { it.id == recetaId }
            recetasList.removeAll { it.id == recetaId }
            recetasList.removeAll { it.id == recetaId }
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Receta eliminada", Toast.LENGTH_SHORT).show()
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, detalleFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        cargarmisRecetas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}