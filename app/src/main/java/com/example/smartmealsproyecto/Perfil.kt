package com.example.smartmealsproyecto

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
import android.widget.EditText

class Perfil : Fragment() {
    private var _binding: FragmentPerfilBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.user.isEnabled = false
        binding.contra.isEnabled = false
        //abrir galeria cuando haya click
        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
        binding.edituser.setOnClickListener{
            binding.user.isEnabled = true
        }
        binding.editcontra.setOnClickListener{
            binding.contra.isEnabled = true
        }
        binding.eliminarcuenta.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Advertencia")
                .setMessage("¿Estás seguro de quieres elimiar tu cuenta?")
                .setPositiveButton("Eliminar") { _, _ ->
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    //limpia actividades
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

                    startActivity(intent)
                    requireActivity().finish() // termina activy_Principal
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                binding.profileImage.setImageURI(uri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}