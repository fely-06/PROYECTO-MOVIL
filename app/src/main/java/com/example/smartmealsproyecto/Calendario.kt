package com.example.smartmealsproyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartmealsproyecto.databinding.FragmentCalendarioBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        var planear: Boolean = false
        var fechaInicio: LocalDate? = null
        var fechaLimite: LocalDate? = null
        binding.buttonMostrarListaCompras.setOnClickListener {
            //val bottomSheet = ListaComprasBottomSheetFragment()
            ///bottomSheet.show(parentFragmentManager, "lista_compras")
        }
        binding.btnplanear.setOnClickListener {
            planear = true;
            Toast.makeText(requireContext(), "Seleccione la fecha de inicio", Toast.LENGTH_SHORT).show()

        }

        binding.calend.setOnDateChangeListener {_, year, month, dayOfMonth ->

            var fechaselect: String
            if (dayOfMonth < 10) {
                fechaselect = "$year-${month + 1}-0$dayOfMonth"
            } else if (month + 1 < 10) {
                fechaselect = "$year-0${month + 1}-$dayOfMonth"
            } else if (dayOfMonth < 10 && month + 1 < 10) {
                fechaselect = "$year-0${month + 1}-0$dayOfMonth"
            } else {
                fechaselect = "$year-${month + 1}-$dayOfMonth"
            }

            if(planear == false) {
                val bottomSheet = DetalleAgenda(fechaselect)
                bottomSheet.show(parentFragmentManager, "detalle_adenda")
            }
            else{
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                //var fechaInicio = LocalDate.now()
                //var fechaLimite = LocalDate.parse(fechaselect,
                if(fechaInicio == null){
                    fechaInicio = LocalDate.parse(fechaselect, formatter)
                    Toast.makeText(requireContext(), "Ahora la fecha Limite", Toast.LENGTH_SHORT).show()
                }
                else if(fechaLimite == null){
                    fechaLimite = LocalDate.parse(fechaselect, formatter)
                    if (fechaLimite != null && fechaLimite!!.isBefore(fechaInicio)) {
                        Toast.makeText(requireContext(), "No se permiten fechas anteriores a la inicial", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val bottomSheet = ListaComprasBottomSheetFragment(fechaLimite.toString(), fechaInicio.toString())
                        bottomSheet.show(parentFragmentManager, "lista_compras")
                        planear = false
                        fechaLimite = null
                        fechaInicio = null
                    }
                }
            }
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