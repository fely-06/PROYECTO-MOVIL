package com.example.smartmealsproyecto

import android.R
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmealsproyecto.databinding.FragmentDetalleRecetaBinding

class DetalleRecetaFragment() : Fragment() {
    private var _binding: FragmentDetalleRecetaBinding? = null
    private val binding get() = _binding!!

    private var recetaId: Int = -1
    private var Global: Boolean = false
    private var receta: Receta? = null
    private var modoEdicion = false

    private lateinit var ingredientesAdapter: IngredientesAdapter
    private val ingredientesList = mutableListOf<Ingrediente>()

    private var onRecetaActualizadaListener: (() -> Unit)? = null
    private var onRecetaEliminadaListener: ((Int) -> Unit)? = null

    companion object {
        private const val ARG_RECETA_ID = "receta_id"
        private const val ARG_GLOBAL = "global"

        fun newInstance(recetaId: Int, esGlobal: Boolean) = DetalleRecetaFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_RECETA_ID, recetaId)
                putBoolean(ARG_GLOBAL, esGlobal)
            }
        }
    }

    fun setOnRecetaActualizadaListener(listener: () -> Unit) {
        onRecetaActualizadaListener = listener
    }

    fun setOnRecetaEliminadaListener(listener: (Int) -> Unit) {
        onRecetaEliminadaListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recetaId = it.getInt(ARG_RECETA_ID)
        }
        arguments?.let {
            Global = it.getBoolean(ARG_GLOBAL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleRecetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarReceta()
        setupIngredientesRecyclerView()
        setupListeners()
        mostrarModoVista()
    }

    private fun cargarReceta() {
        receta = RecetasTotales.todasLasRecetas.find { it.id == recetaId }
        receta?.let {
            binding.editTextNombre.setText(it.nombre)
            binding.editTextTiempo.setText(it.tiempoMinutos.toString())
            binding.editTextDescripcion.setText(it.descripcion)

            ingredientesList.clear()
            ingredientesList.addAll(it.ingredientes)
        }
    }

    private fun setupIngredientesRecyclerView() {
        ingredientesAdapter = IngredientesAdapter(ingredientesList) { ingrediente ->
            if (modoEdicion) {
                ingredientesList.remove(ingrediente)
                ingredientesAdapter.notifyDataSetChanged()
            }
        }

        binding.recyclerIngredientes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ingredientesAdapter
        }
    }

    private fun setupListeners() {
        binding.buttonEditar.setOnClickListener {
            cambiarModoEdicion()
        }

        binding.buttonEliminar.setOnClickListener {
            mostrarDialogoEliminar()
        }

        binding.buttonAgregarIngrediente.setOnClickListener {
            agregarIngrediente()
        }

        binding.buttonGuardar.setOnClickListener {
            guardarCambios()
        }

        binding.buttonCancelar.setOnClickListener {
            if (modoEdicion) {
                cargarReceta()
                mostrarModoVista()}
        }

        binding.buttonVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun cambiarModoEdicion() {
        modoEdicion = true
        mostrarModoEdicion()
    }

    private fun mostrarModoVista() {
        modoEdicion = false
        binding.editTextNombre.isEnabled = false
        binding.editTextTiempo.isEnabled = false
        binding.editTextDescripcion.isEnabled = false
        binding.editTextIngredienteNombre.isEnabled = false
        binding.editTextIngredienteCantidad.isEnabled = false
        binding.editTextIngredienteUnidad.isEnabled = false

        binding.layoutAgregarIngrediente.visibility = View.GONE
        binding.buttonGuardar.visibility = View.GONE
        binding.buttonCancelar.visibility = View.GONE
        if(Global == true){
            binding.buttonEditar.visibility = View.GONE
            binding.buttonEliminar.visibility = View.GONE
        }
        if(Global == false){
            binding.buttonEditar.visibility = View.VISIBLE
            binding.buttonEliminar.visibility = View.VISIBLE
        }
        binding.buttonVolver.visibility = View.VISIBLE
    }

    private fun mostrarModoEdicion() {
        modoEdicion = true

        binding.editTextNombre.isEnabled = true
        binding.editTextTiempo.isEnabled = true
        binding.editTextDescripcion.isEnabled = true
        binding.editTextIngredienteNombre.isEnabled = true
        binding.editTextIngredienteCantidad.isEnabled = true
        binding.editTextIngredienteUnidad.isEnabled = true

        binding.layoutAgregarIngrediente.visibility = View.VISIBLE
        binding.buttonGuardar.visibility = View.VISIBLE
        binding.buttonEditar.visibility = View.GONE
        binding.buttonEliminar.visibility = View.GONE
        binding.buttonVolver.visibility = View.GONE
        binding.buttonCancelar.visibility = View.VISIBLE
        binding.buttonCancelar.text = "Cancelar"
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

    private fun guardarCambios() {
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
            Toast.makeText(requireContext(), "Debe haber al menos un ingrediente", Toast.LENGTH_SHORT).show()
            return
        }

        receta?.let {
            it.nombre = nombre
            it.tiempoMinutos = tiempo
            it.descripcion = descripcion
            it.ingredientes.clear()
            it.ingredientes.addAll(ingredientesList)
        }

        onRecetaActualizadaListener?.invoke()
        Toast.makeText(requireContext(), "Receta actualizada", Toast.LENGTH_SHORT).show()
        mostrarModoVista()
    }

    private fun mostrarDialogoEliminar() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar receta")
            .setMessage("¿Estás seguro de que deseas eliminar esta receta?")
            .setPositiveButton("Eliminar") { _, _ ->
                onRecetaEliminadaListener?.invoke(recetaId)
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}