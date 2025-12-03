package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmealsproyecto.databinding.FragmentRecetasBinding
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
class Recetas : Fragment() {

    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!

    companion object {
        val recetasList = mutableListOf<Receta2>()
        private val recetasListOriginal = mutableListOf<Receta2>()
    }

    private lateinit var adapter: RecetasAdapt

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
        setupFab()
        setupSearch()
        setupSortButtons()
    }

    private fun setupRecyclerView() {
        adapter = RecetasAdapt(RecetasTotales.misRecetas, onRecetaClick = { receta -> abrirDetalleReceta(receta)})
        binding.rec.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@Recetas.adapter
        }
    }
    private fun abrirDetalleReceta(receta: Receta2) {
        val detalleFragment = DetalleRecetaFragment.newInstance(receta.id, false)
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, detalleFragment)
            .addToBackStack(null)
            .commit()
    }
    private fun cargarmisRecetas() {
        lifecycleScope.launch {
            try {
                val crud = ClaseCRUD(requireContext())
                crud.iniciarBD()
                crud.obtenerMisRecetas(RecetasTotales.misRecetas, recetasListOriginal)
                recetasList.clear();
                recetasList.addAll(RecetasTotales.misRecetas)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al cargar tus recetas", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val fragment = NuevaRecetaFragment.newInstance()
            fragment.setOnRecetaGuardadaListener {
                cargarmisRecetas()
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
    private fun filterRecetas(query: String) {
        val filtered = if (query.isEmpty()) {
            recetasListOriginal
        } else {
            recetasListOriginal.filter {
                it.nombre.contains(query, ignoreCase = true)
            }
        }
        RecetasTotales.misRecetas.clear()
        RecetasTotales.misRecetas.addAll(filtered)
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
            RecetasTotales.misRecetas.sortBy { it.nombre }
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Ordenado A-Z", Toast.LENGTH_SHORT).show()
        }

        binding.iconSortZA.setOnClickListener {
            RecetasTotales.misRecetas.sortByDescending { it.nombre }
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Ordenado Z-A", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}