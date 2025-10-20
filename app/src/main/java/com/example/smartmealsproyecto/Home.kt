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
import java.io.IOException

class Home : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecetasGAdap
    private val recetasList = mutableListOf<Receta2>()
    private val recetasListOriginal = mutableListOf<Receta2>()

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
        setupRecyclerView()
        loadRecetasFromDatabase()
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

    private fun loadRecetasFromDatabase() {
        // Obtener instancia del helper
        val dbHelper = DatabaseHelper.getInstance(requireContext())
        dbHelper.createDatabase()
        try {
            dbHelper.createDatabase() // Asegura que la BD esté copiada
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error al cargar la base de datos", Toast.LENGTH_LONG).show()
            return
        }

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT idReceta, idUsuario, nombre, descripcion, tiempoPreparacion, esGlobal, " +
                "favorita FROM Receta WHERE esGlobal = 1", null)

        recetasList.clear()
        recetasListOriginal.clear()

        with(cursor) {
            if (moveToFirst()) {
                do {
                    val id = getInt(getColumnIndexOrThrow("idReceta"))
                    val idUsuario = getInt(getColumnIndexOrThrow("idUsuario"))
                    val nombre = getString(getColumnIndexOrThrow("nombre"))
                    val descripcion = getString(getColumnIndexOrThrow("descripcion")) ?: ""
                    val tiempo = getInt(getColumnIndexOrThrow("tiempoPreparacion"))
                    val esGlobal = getInt(getColumnIndexOrThrow("esGlobal")) == 1
                    val favorita = getInt(getColumnIndexOrThrow("favorita")) == 1

                    val receta = Receta2(id, idUsuario, nombre, descripcion, tiempo, esGlobal, favorita)
                    recetasList.add(receta)
                    recetasListOriginal.add(receta)
                } while (cursor.moveToNext())
            }
        }
            cursor.close()
            db.close()
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

    private fun abrirDetalleReceta(receta: Receta2) {
        val detalleFragment = DetalleRecetaFragment.newInstance(receta.id, true)
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, detalleFragment)
            .addToBackStack(null)
            .commit()
    }
    private fun AgregarReceta(receta: Receta2){
        val misRecetas = RecetasTotales.misRecetas
        if (receta.favorita) {
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
