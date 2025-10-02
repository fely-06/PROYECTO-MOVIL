package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmealsproyecto.databinding.FragmentRecetasBinding

class Recetas : Fragment() {
    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecetasAdapt
    private val recetasList = mutableListOf<Receta>()
    private val recetasListOriginal = mutableListOf<Receta>()

    companion object {
        private var nextId = 1
        val recetasGlobales = mutableListOf<Receta>()
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
        setupData()
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

    private fun setupData() {
        if (recetasGlobales.isEmpty()) {
            recetasGlobales.apply {
                add(Receta(
                    nextId++,
                    "Ensalada César",
                    15,
                    "Mezcla lechuga romana con aderezo césar, crutones y queso parmesano.",
                    mutableListOf(
                        Ingrediente("Lechuga romana", "1", "pieza"),
                        Ingrediente("Aderezo césar", "100", "ml"),
                        Ingrediente("Crutones", "50", "g"),
                        Ingrediente("Queso parmesano", "30", "g")
                    )
                ))
                add(Receta(
                    nextId++,
                    "Pollo a la Brasa",
                    45,
                    "Pollo marinado con especias y cocido al horno hasta dorar.",
                    mutableListOf(
                        Ingrediente("Pollo entero", "1", "pieza"),
                        Ingrediente("Paprika", "2", "cucharadas"),
                        Ingrediente("Ajo", "4", "dientes"),
                        Ingrediente("Aceite", "50", "ml")
                    )
                ))
                add(Receta(
                    nextId++,
                    "Pasta Carbonara",
                    30,
                    "Pasta con salsa de huevo, queso parmesano y tocino.",
                    mutableListOf(
                        Ingrediente("Pasta", "400", "g"),
                        Ingrediente("Tocino", "150", "g"),
                        Ingrediente("Huevos", "3", "piezas"),
                        Ingrediente("Queso parmesano", "100", "g")
                    )
                ))
            }
        }

        recetasList.clear()
        recetasList.addAll(recetasGlobales)
        recetasListOriginal.clear()
        recetasListOriginal.addAll(recetasGlobales)
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
            recetasListOriginal
        } else {
            recetasListOriginal.filter {
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
                receta.id = nextId++
                recetasGlobales.add(receta)
                recetasListOriginal.add(receta)

                val query = binding.editTextSearch.text.toString().trim()
                filterRecetas(query)

                Toast.makeText(requireContext(), "Receta guardada", Toast.LENGTH_SHORT).show()
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun abrirDetalleReceta(receta: Receta) {
        val detalleFragment = DetalleRecetaFragment.newInstance(receta.id)
        detalleFragment.setOnRecetaActualizadaListener {
            adapter.notifyDataSetChanged()
        }
        detalleFragment.setOnRecetaEliminadaListener { recetaId ->
            recetasGlobales.removeAll { it.id == recetaId }
            recetasListOriginal.removeAll { it.id == recetaId }
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
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}