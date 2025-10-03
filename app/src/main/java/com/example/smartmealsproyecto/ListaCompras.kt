package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.smartmealsproyecto.databinding.FragmentListaComprasBottomSheetBinding

class ListaComprasBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentListaComprasBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaComprasBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Aquí irá la lógica real más adelante
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}