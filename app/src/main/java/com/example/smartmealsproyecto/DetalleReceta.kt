package com.example.smartmealsproyecto

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmealsproyecto.databinding.FragmentDetalleRecetaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

class DetalleRecetaFragment : Fragment() {

    private var _binding: FragmentDetalleRecetaBinding? = null
    private val binding get() = _binding!!

    private var recetaId: Int = -1
    private var Global: Boolean = false
    private var receta: Receta2? = null
    private var modoEdicion = false
    private var puedeEditar = false // Nueva variable

    private lateinit var ingredientesAdapter: IngredientesAdapter
    private val ingredientesList = mutableListOf<Ingrediente>()

    private var onRecetaActualizadaListener: (() -> Unit)? = null
    private var onRecetaEliminadaListener: ((Int) -> Unit)? = null
    private var currentPhotoUri: Uri? = null
    private var currentPhotoFile: File? = null
    private var nuevaImagenRuta: String? = null
    private var imagenCambiada: Boolean = false

    // Launchers
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(requireContext(), "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            nuevaImagenRuta = currentPhotoFile?.absolutePath
            imagenCambiada = true
            displaySelectedImage()
            Toast.makeText(requireContext(), "Foto capturada", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentPhotoUri = uri
                imagenCambiada = true
                displaySelectedImage()
                Toast.makeText(requireContext(), "Imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        cargarRecetaDesdeBD()
        setupIngredientesRecyclerView()
        setupListeners()
        mostrarModoVista()
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
            if (puedeEditar) {
                cambiarModoEdicion()
            } else {
                Toast.makeText(
                    requireContext(),
                    "No tienes permisos para editar esta receta",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.buttonEliminar.setOnClickListener {
            if (puedeEditar) {
                mostrarDialogoEliminar()
            } else {
                Toast.makeText(
                    requireContext(),
                    "No tienes permisos para eliminar esta receta",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.buttonAgregarIngrediente.setOnClickListener { agregarIngrediente() }
        binding.buttonGuardar.setOnClickListener { guardarCambios() }
        binding.buttonCancelar.setOnClickListener {
            if (modoEdicion) {
                cargarRecetaDesdeBD()
                mostrarModoVista()
            }
        }
        binding.buttonVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.buttonCambiarImagen.setOnClickListener {
            if (modoEdicion) {
                showImagePickerDialog()
            }
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
        binding.buttonCambiarImagen.visibility = View.GONE

        // Mostrar botones de editar/eliminar solo si tiene permisos
        if (puedeEditar) {
            binding.buttonEditar.visibility = View.VISIBLE
            binding.buttonEliminar.visibility = View.VISIBLE
        } else {
            binding.buttonEditar.visibility = View.GONE
            binding.buttonEliminar.visibility = View.GONE
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
        binding.buttonCambiarImagen.visibility = View.VISIBLE
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
            Toast.makeText(
                requireContext(),
                "Completa todos los campos del ingrediente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun mostrarDialogoEliminar() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar receta")
            .setMessage("¿Estás seguro de que deseas eliminar esta receta?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarReceta() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun guardarCambios() {
        val nombre = binding.editTextNombre.text.toString().trim()
        val tiempoStr = binding.editTextTiempo.text.toString().trim()
        val descripcion = binding.editTextDescripcion.text.toString().trim()

        if (nombre.isEmpty() || tiempoStr.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre y tiempo son obligatorios", Toast.LENGTH_SHORT).show()
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

        lifecycleScope.launch {
            try {
                val crud = ClaseCRUD(requireContext())

                var rutaImagenFinal = receta?.imagenRuta

                if (imagenCambiada) {
                    if (!receta?.imagenRuta.isNullOrEmpty()) {
                        ImageHelper.deleteImage(receta?.imagenRuta)
                    }

                    rutaImagenFinal = if (currentPhotoUri != null) {
                        ImageHelper.saveImageToInternalStorage(
                            requireContext(),
                            currentPhotoUri!!,
                            recetaId
                        )
                    } else {
                        null
                    }
                }

                val recetaActualizada = receta?.copy(
                    nombre = nombre,
                    descripcion = descripcion.ifEmpty { null },
                    tiempoPreparacion = tiempo,
                    imagenRuta = rutaImagenFinal
                ) ?: return@launch

                val exito = crud.actualizarReceta(recetaActualizada, ingredientesList)

                if (exito) {
                    imagenCambiada = false
                    onRecetaActualizadaListener?.invoke()
                    mostrarModoVista()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun eliminarReceta() {
        lifecycleScope.launch {
            val crud = ClaseCRUD(requireContext())
            crud.iniciarBD()
            val exito = crud.eliminarReceta(recetaId)
            if (exito) {
                onRecetaEliminadaListener?.invoke(recetaId)
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun cargarRecetaDesdeBD() {
        lifecycleScope.launch {
            var recetaCargada: Receta2? = null
            val ingredientesTemp = mutableListOf<Ingrediente>()

            withContext(Dispatchers.IO) {
                val crud = ClaseCRUD(requireContext())
                crud.iniciarBD()
                val db = crud.dbHelper.readableDatabase

                val cursorReceta = db.rawQuery(
                    """
                SELECT r.idReceta, r.idUsuario, r.nombre, r.descripcion, 
                       r.tiempoPreparacion, r.esGlobal, r.imagenRuta,
                       CASE WHEN rg.idReceta IS NOT NULL THEN 1 ELSE 0 END AS favorita
                FROM Receta r
                LEFT JOIN RecetaGuardada rg 
                    ON r.idReceta = rg.idReceta 
                    AND rg.idUsuario = ?
                WHERE r.idReceta = ?
                """.trimIndent(),
                    arrayOf(ClaseUsuario.iduser.toString(), recetaId.toString())
                )

                if (cursorReceta.moveToFirst()) {
                    val id = cursorReceta.getInt(cursorReceta.getColumnIndexOrThrow("idReceta"))
                    val idUsuario = cursorReceta.getInt(cursorReceta.getColumnIndexOrThrow("idUsuario"))
                    val nombre = cursorReceta.getString(cursorReceta.getColumnIndexOrThrow("nombre"))
                    val descripcion = cursorReceta.getString(cursorReceta.getColumnIndexOrThrow("descripcion")) ?: ""
                    val tiempo = cursorReceta.getInt(cursorReceta.getColumnIndexOrThrow("tiempoPreparacion"))
                    val esGlobal = cursorReceta.getInt(cursorReceta.getColumnIndexOrThrow("esGlobal")) == 1
                    val imagenRuta = cursorReceta.getString(cursorReceta.getColumnIndexOrThrow("imagenRuta"))
                    val favorita = cursorReceta.getInt(cursorReceta.getColumnIndexOrThrow("favorita")) == 1

                    recetaCargada = Receta2(id, idUsuario, nombre, descripcion, tiempo, esGlobal, favorita, imagenRuta)

                    val cursorIng = db.rawQuery(
                        "SELECT nombre, cantidad, unidad FROM Ingrediente WHERE idReceta = ?",
                        arrayOf(recetaId.toString())
                    )

                    with(cursorIng) {
                        if (moveToFirst()) {
                            do {
                                val nom = getString(getColumnIndexOrThrow("nombre"))
                                val cant = getString(getColumnIndexOrThrow("cantidad"))
                                val uni = getString(getColumnIndexOrThrow("unidad"))
                                ingredientesTemp.add(Ingrediente(nom, cant, uni))
                            } while (moveToNext())
                        }
                        close()
                    }
                }
                cursorReceta.close()
            }

            receta = recetaCargada
            ingredientesList.clear()
            ingredientesList.addAll(ingredientesTemp)

            receta?.let { rec ->
                // Verificar permisos de edición
                puedeEditar = rec.puedeEditar(ClaseUsuario.iduser)

                binding.editTextNombre.setText(rec.nombre)
                binding.editTextTiempo.setText(rec.tiempoPreparacion.toString())
                binding.editTextDescripcion.setText(rec.descripcion)

                if (!rec.imagenRuta.isNullOrEmpty()) {
                    val bitmap = ImageHelper.loadImageFromPath(rec.imagenRuta)
                    if (bitmap != null) {
                        binding.imageViewReceta.setImageBitmap(bitmap)
                        binding.imageViewReceta.visibility = View.VISIBLE
                        binding.textViewNoImagen.visibility = View.GONE
                    } else {
                        binding.imageViewReceta.visibility = View.GONE
                        binding.textViewNoImagen.visibility = View.VISIBLE
                    }
                } else {
                    binding.imageViewReceta.visibility = View.GONE
                    binding.textViewNoImagen.visibility = View.VISIBLE
                }
            }

            ingredientesAdapter.notifyDataSetChanged()

            // Actualizar UI según permisos
            mostrarModoVista()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería", "Eliminar foto")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cambiar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> checkGalleryPermissionAndOpen()
                    2 -> removeImage()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkGalleryPermissionAndOpen() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                galleryPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openCamera() {
        try {
            currentPhotoFile = ImageHelper.createImageFile(requireContext())
            currentPhotoUri = ImageHelper.getUriForFile(requireContext(), currentPhotoFile!!)
            cameraLauncher.launch(currentPhotoUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al abrir cámara", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun displaySelectedImage() {
        currentPhotoUri?.let { uri ->
            binding.imageViewReceta.setImageURI(uri)
            binding.imageViewReceta.visibility = View.VISIBLE
            binding.textViewNoImagen.visibility = View.GONE
        }
    }

    private fun removeImage() {
        currentPhotoUri = null
        nuevaImagenRuta = null
        imagenCambiada = true
        binding.imageViewReceta.setImageDrawable(null)
        binding.imageViewReceta.visibility = View.GONE
        binding.textViewNoImagen.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Imagen eliminada", Toast.LENGTH_SHORT).show()
    }
}