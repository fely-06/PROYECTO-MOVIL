package com.example.smartmealsproyecto

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartmealsproyecto.databinding.FragmentPerfilBinding
import android.content.Intent
import android.provider.MediaStore
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File

class Perfil : Fragment() {
    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private var currentPhotoUri: Uri? = null
    private var currentPhotoFile: File? = null
    private var imagenCambiada: Boolean = false

    // Launchers para permisos
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            android.widget.Toast.makeText(requireContext(), "Permiso de cámara denegado", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            android.widget.Toast.makeText(requireContext(), "Permiso de galería denegado", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Launchers para cámara y galería
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            imagenCambiada = true
            displaySelectedImage()
            guardarImagenPerfil()
            android.widget.Toast.makeText(requireContext(), "Foto capturada", android.widget.Toast.LENGTH_SHORT).show()
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
                guardarImagenPerfil()
                android.widget.Toast.makeText(requireContext(), "Imagen seleccionada", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val crud = ClaseCRUD(requireContext())
        crud.iniciarBD()

        binding.user.setText(ClaseUsuario.nombre)
        binding.contra.setText(ClaseUsuario.contras)
        binding.user.isEnabled = false
        binding.contra.isEnabled = false

        // Cargar imagen de perfil si existe
        cargarImagenPerfil()

        // Click en imagen de perfil para cambiarla
        binding.profileImage.setOnClickListener {
            showImagePickerDialog()
        }

        binding.editcontra.setOnClickListener {
            binding.contra.isEnabled = true
            binding.btncancel.isVisible = true
            binding.btncrear.isVisible = true
        }

        binding.btncancel.setOnClickListener() {
            binding.btncancel.isVisible = false
            binding.btncrear.isVisible = false
            binding.user.setText(ClaseUsuario.nombre)
            binding.contra.setText(ClaseUsuario.contras)
            binding.contra.isEnabled = false
        }

        binding.btncrear.setOnClickListener() {
            var v: Boolean = false
            var c: String = binding.contra.text.toString()
            lifecycleScope.launch {
                v = crud.actualizarContrasenaUsuario(c)
                if (v == true) {
                    ClaseUsuario.contras = c
                }
            }
            binding.contra.setText(c)
            binding.contra.isEnabled = false
            binding.btncancel.isVisible = false
            binding.btncrear.isVisible = false
        }

        binding.eliminarcuenta.setOnClickListener {
            var e: Boolean = false
            AlertDialog.Builder(requireContext())
                .setTitle("Advertencia")
                .setMessage("¿Estás seguro de quieres elimiar tu cuenta?")
                .setPositiveButton("Eliminar") { _, _ ->
                    lifecycleScope.launch {
                        e = crud.eliminarUsuario()
                    }
                    if (e == true) {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería", "Eliminar foto")
        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar foto de perfil")
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
            android.widget.Toast.makeText(requireContext(), "Error al abrir cámara", android.widget.Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun displaySelectedImage() {
        currentPhotoUri?.let { uri ->
            binding.profileImage.setImageURI(uri)
        }
    }

    private fun removeImage() {
        lifecycleScope.launch {
            val crud = ClaseCRUD(requireContext())
            crud.iniciarBD()

            // Eliminar imagen anterior si existe
            if (!ClaseUsuario.fotoPerfil.isNullOrEmpty()) {
                ImageHelper.deleteImage(ClaseUsuario.fotoPerfil)
            }

            // Actualizar en BD
            crud.actualizarFotoPerfilUsuario("")
            ClaseUsuario.fotoPerfil = ""

            currentPhotoUri = null
            imagenCambiada = false

            // Restaurar imagen por defecto
            binding.profileImage.setImageResource(R.drawable.fondomenu)
            android.widget.Toast.makeText(requireContext(), "Foto eliminada", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarImagenPerfil() {
        lifecycleScope.launch {
            val crud = ClaseCRUD(requireContext())
            crud.iniciarBD()

            val fotoPerfil = crud.obtenerFotoPerfilUsuario()
            ClaseUsuario.fotoPerfil = fotoPerfil

            if (!fotoPerfil.isNullOrEmpty()) {
                val bitmap = ImageHelper.loadImageFromPath(fotoPerfil)
                if (bitmap != null) {
                    binding.profileImage.setImageBitmap(bitmap)
                } else {
                    binding.profileImage.setImageResource(R.drawable.fondomenu)
                }
            } else {
                binding.profileImage.setImageResource(R.drawable.fondomenu)
            }
        }
    }

    private fun guardarImagenPerfil() {
        lifecycleScope.launch {
            try {
                val crud = ClaseCRUD(requireContext())
                crud.iniciarBD()

                // Eliminar imagen anterior si existe
                if (!ClaseUsuario.fotoPerfil.isNullOrEmpty()) {
                    ImageHelper.deleteImage(ClaseUsuario.fotoPerfil)
                }

                // Guardar nueva imagen
                val rutaImagen = if (currentPhotoUri != null) {
                    ImageHelper.saveImageToInternalStorage(
                        requireContext(),
                        currentPhotoUri!!,
                        ClaseUsuario.iduser
                    )
                } else {
                    null
                }

                if (rutaImagen != null) {
                    crud.actualizarFotoPerfilUsuario(rutaImagen)
                    ClaseUsuario.fotoPerfil = rutaImagen
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Foto de perfil actualizada",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    requireContext(),
                    "Error al guardar foto: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}