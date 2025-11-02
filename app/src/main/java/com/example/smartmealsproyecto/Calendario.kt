package com.example.smartmealsproyecto

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartmealsproyecto.databinding.FragmentCalendarioBinding
import java.util.*

class Calendario : Fragment() {

    private var _binding: FragmentCalendarioBinding? = null
    private val binding get() = _binding!!
    private val calendario = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val locale = Locale("es", "ES")
        Locale.setDefault(locale)

        val config = requireContext().resources.configuration
        config.setLocale(locale)

        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonMostrarListaCompras.setOnClickListener {
            val bottomSheet = ListaComprasBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, "lista_compras")
        }

        binding.calend.setOnDateChangeListener {_, year, month, dayOfMonth ->
            var fechaselect: String
            if(dayOfMonth<10){
                fechaselect = "$year-${month+1}-0$dayOfMonth"
            }
            else if(month+1<10){
                fechaselect = "$year-0${month+1}-$dayOfMonth"
            }
            else if(dayOfMonth<10&&month+1<10){
                fechaselect = "$year-0${month+1}-0$dayOfMonth"
            }
            else{
                fechaselect = "$year-${month+1}-$dayOfMonth"
            }
            val bottomSheet = DetalleAgenda(fechaselect)
            bottomSheet.show(parentFragmentManager, "detalle_adenda")

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = Calendario()
    }
}