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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmealsproyecto.databinding.FragmentNuevaRecetaBinding
import kotlinx.coroutines.launch
import java.io.File

class NuevaRecetaFragment : Fragment() {
    private var _binding: FragmentNuevaRecetaBinding? = null
    private val binding get() = _binding!!

    private val ingredientesList = mutableListOf<Ingrediente>()
    private lateinit var ingredientesAdapter: IngredientesAdapter
    private var onRecetaGuardadaListener: ((Receta2) -> Unit)? = null

    private var currentPhotoUri: Uri? = null
    private var currentPhotoFile: File? = null
    private var selectedImagePath: String? = null
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
            selectedImagePath = currentPhotoFile?.absolutePath
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
                displaySelectedImage()
                Toast.makeText(requireContext(), "Imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        binding.buttonAgregarImagen.setOnClickListener {
            showImagePickerDialog()
        }

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

    private fun showImagePickerDialog() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería", "Eliminar foto")
        AlertDialog.Builder(requireContext())
            .setTitle("Agregar imagen")
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
        selectedImagePath = null
        binding.imageViewReceta.setImageDrawable(null)
        binding.imageViewReceta.visibility = View.GONE
        binding.textViewNoImagen.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Imagen eliminada", Toast.LENGTH_SHORT).show()
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
                    id = 0,
                    idUsuario = ClaseUsuario.iduser,
                    nombre = nombre,
                    descripcion = descripcion,
                    tiempoPreparacion = tiempo,
                    esGlobal = false,
                    favorita = false,
                    imagenRuta = null
                )

                val idReceta = crud.crearReceta(nuevaReceta, ingredientesList)

                if (idReceta != -1L) {
                    var rutaImagen: String? = null
                    currentPhotoUri?.let { uri ->
                        rutaImagen = ImageHelper.saveImageToInternalStorage(
                            requireContext(),
                            uri,
                            idReceta.toInt()
                        )
                        if (rutaImagen != null) {
                            crud.actualizarImagenReceta(idReceta.toInt(), rutaImagen!!)
                        }
                    }

                    val recetaGuardada = nuevaReceta.copy(
                        id = idReceta.toInt(),
                        imagenRuta = rutaImagen
                    )

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