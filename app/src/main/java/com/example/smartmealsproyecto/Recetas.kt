package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmealsproyecto.databinding.FragmentRecetasBinding

class Recetas : Fragment() {

    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecetasAdapt
    private var recetasList = mutableListOf<Receta>()
    private var recetasListOriginal = mutableListOf<Receta>()

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
        setupData()
        setupSearch()
        setupSortButtons()
        setupFab()
    }

    private fun setupRecyclerView() {
        recyclerView = binding.rec
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecetasAdapt(recetasList)
        recyclerView.adapter = adapter
    }

    private fun setupData() {
        recetasList.apply {
            add(Receta("Ensalada César", 15))
            add(Receta("Pollo a la Brasa", 45))
            add(Receta("Pasta Carbonara", 30))
            add(Receta("Hamburguesa Casera", 25))
            add(Receta("Sopa de Pollo", 20))
            add(Receta("Tacos al Pastor", 35))
            add(Receta("Pizza Margarita", 40))
            add(Receta("Arroz con Pollo", 50))
        }
        recetasListOriginal.addAll(recetasList)
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
        // Ordenar A-Z
        binding.iconSortAZ.setOnClickListener {
            recetasList.sortBy { it.nombre }
            adapter = RecetasAdapt(recetasList)
            recyclerView.adapter = adapter
            Toast.makeText(requireContext(), "Ordenado A-Z", Toast.LENGTH_SHORT).show()
        }

        // Ordenar Z-A
        binding.iconSortZA.setOnClickListener {
            recetasList.sortByDescending { it.nombre }
            adapter = RecetasAdapt(recetasList)
            recyclerView.adapter = adapter
            Toast.makeText(requireContext(), "Ordenado Z-A", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterRecetas(query: String) {
        val filtered = if (query.isEmpty()) {
            recetasListOriginal
        } else {
            recetasListOriginal.filter { it.nombre.contains(query, ignoreCase = true) }
        }
        recetasList.clear()
        recetasList.addAll(filtered)
        adapter = RecetasAdapt(recetasList)
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Agregar nueva receta", Toast.LENGTH_SHORT).show()
            // Aquí puedes abrir un diálogo o navegar a otra pantalla
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}