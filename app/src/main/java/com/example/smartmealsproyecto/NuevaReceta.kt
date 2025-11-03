package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmealsproyecto.databinding.FragmentNuevaRecetaBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
class NuevaRecetaFragment : Fragment() {
    private var _binding: FragmentNuevaRecetaBinding? = null
    private val binding get() = _binding!!

    private val ingredientesList = mutableListOf<Ingrediente>()
    private lateinit var ingredientesAdapter: IngredientesAdapter

    private var onRecetaGuardadaListener: ((Receta2) -> Unit)? = null

    companion object {
        fun newInstance() = NuevaRecetaFragment()
    }

    fun setOnRecetaGuardadaListener(listener: (Receta2) -> Unit) {
        onRecetaGuardadaListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevaRecetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupIngredientesRecyclerView()
        setupListeners()
    }

    private fun setupIngredientesRecyclerView() {
        ingredientesAdapter = IngredientesAdapter(ingredientesList) { ingrediente ->
            ingredientesList.remove(ingrediente)
            ingredientesAdapter.notifyDataSetChanged()
        }

        binding.recyclerIngredientes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ingredientesAdapter
        }
    }

    private fun setupListeners() {
        binding.buttonAgregarIngrediente.setOnClickListener {
            agregarIngrediente()
        }

        binding.buttonGuardar.setOnClickListener {
            guardarReceta()
        }

        binding.buttonCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun agregarIngrediente() {
        val nombre = binding.editTextIngredienteNombre.text.toString().trim()
        val cantidad = binding.editTextIngredienteCantidad.text.toString().trim()
        val unidad = binding.editTextIngredienteUnidad.text.toString().trim()

        if (nombre.isNotEmpty() && cantidad.isNotEmpty() && unidad.isNotEmpty()) {
            val ingrediente = Ingrediente(nombre, cantidad, unidad)
            ingredientesList.add(ingrediente)
            ingredientesAdapter.notifyDataSetChanged()

            binding.editTextIngredienteNombre.text?.clear()
            binding.editTextIngredienteCantidad.text?.clear()
            binding.editTextIngredienteUnidad.text?.clear()
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos del ingrediente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarReceta() {
        val nombre = binding.editTextNombre.text.toString().trim()
        val tiempoStr = binding.editTextTiempo.text.toString().trim()
        val descripcion = binding.editTextDescripcion.text.toString().trim()

        if (nombre.isEmpty() || tiempoStr.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val tiempo = tiempoStr.toIntOrNull()
        if (tiempo == null || tiempo <= 0) {
            Toast.makeText(requireContext(), "Tiempo inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (ingredientesList.isEmpty()) {
            Toast.makeText(requireContext(), "Agrega al menos un ingrediente", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val crud = ClaseCRUD(requireContext())
                crud.iniciarBD()

                val nuevaReceta = Receta2(
                    id = 0, // SQLite auto-genera el ID
                    idUsuario = ClaseUsuario.iduser,
                     nombre = nombre,
                    descripcion = descripcion,
                    tiempoPreparacion = tiempo,
                    esGlobal = false,
                    favorita = false
                )
                val idReceta = crud.crearReceta(nuevaReceta, ingredientesList)

                if (idReceta != -1L) {
                    val recetaGuardada = nuevaReceta.copy(id = idReceta.toInt())
                    onRecetaGuardadaListener?.invoke(recetaGuardada)
                    parentFragmentManager.popBackStack()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}