package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmealsproyecto.databinding.FragmentHomeBinding
import com.example.smartmealsproyecto.databinding.FragmentRecetasBinding

class Home : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecetasGAdap
    private val recetasList = mutableListOf<Receta>()
    private val recetasListOriginal = mutableListOf<Receta>()

    /*companion object {
        private var nextId = 1
        val recetasGlobales = mutableListOf<Receta>()
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RecetasTotales.inicializarRecetas()
        setupRecyclerView()
        setupData()
        setupSearch()
        setupSortButtons()
    }

    private fun setupRecyclerView() {
        adapter = RecetasGAdap(
            recetas = recetasList,
            onRecetaClick = { receta -> abrirDetalleReceta(receta) },
            onCheckBoxCheck = { receta -> AgregarReceta(receta) }
        )
        binding.recG.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@Home.adapter
        }
    }

    private fun setupData() {
        recetasList.clear()
        recetasList.addAll(RecetasTotales.recetasGlobales)
        recetasListOriginal.clear()
        recetasListOriginal.addAll(RecetasTotales.todasLasRecetas)
        adapter.notifyDataSetChanged()
        /*if (recetasGlobales.isEmpty()) {
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
        adapter.notifyDataSetChanged()*/
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

    private fun abrirDetalleReceta(receta: Receta) {
        val detalleFragment = DetalleRecetaFragment.newInstance(receta.id, true)
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, detalleFragment)
            .addToBackStack(null)
            .commit()
    }
    private fun AgregarReceta(receta: Receta){
        val misRecetas = RecetasTotales.misRecetas
        if (receta.seleccionada) {
            if (!misRecetas.any { it.id == receta.id }) {
                misRecetas.add(receta)
                Toast.makeText(requireContext(), "${receta.nombre} agregada a Mis Recetas", Toast.LENGTH_SHORT).show()
            }
        } else {
            misRecetas.removeAll { it.id == receta.id }
            Toast.makeText(requireContext(), "${receta.nombre} eliminada de Mis Recetas", Toast.LENGTH_SHORT).show()
        }
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
