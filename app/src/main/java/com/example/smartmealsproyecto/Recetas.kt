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
        if (ClaseUsuario.iduser == 0) {
            Toast.makeText(requireContext(), "Inicia sesión para ver tus recetas", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val crud = ClaseCRUD(requireContext())
                crud.iniciarBD()
                crud.obtenerMisRecetas(recetasList,recetasList)
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