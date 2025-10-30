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

    companion object {
        val recetasList = mutableListOf<Receta2>()
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
    }

    private fun setupRecyclerView() {
        adapter = RecetasAdapt(recetasList) { /* no usado aún */ }
        binding.rec.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun cargarmisRecetas() {
        // Solo para mostrar que la receta se guardó → recarga desde BD
        recetasList.clear()
        val crud = ClaseCRUD(requireContext())
        crud.iniciarBD()

        val db = crud.dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT idReceta, idUsuario, nombre, descripcion, tiempoPreparacion, esGlobal, favorita " +
                    "FROM Receta WHERE esGlobal = 0 AND idUsuario = ?",
            arrayOf("1") // ← usa tu ID real
        )

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

                    recetasList.add(Receta2(id, idUsuario, nombre, descripcion, tiempo, esGlobal, favorita))
                } while (moveToNext())
            }
            close()
        }
        adapter.notifyDataSetChanged()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val fragment = NuevaRecetaFragment.newInstance()
            fragment.setOnRecetaGuardadaListener {
                // Recargar lista desde BD
                cargarmisRecetas()
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}