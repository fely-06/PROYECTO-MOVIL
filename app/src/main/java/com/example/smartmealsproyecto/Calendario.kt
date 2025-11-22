package com.example.smartmealsproyecto

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.smartmealsproyecto.databinding.FragmentCalendarioBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val crud = ClaseCRUD(requireContext())

        cargarGrafico()
        // Obtener y mostrar listas
        lifecycleScope.launch {
            val nombres = crud.obtenerNombresListasPorUsuario(ClaseUsuario.iduser)
            val nombresConPlaceholder = listOf("Seleccionar lista...") + nombres

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                nombresConPlaceholder
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerListas.adapter = adapter
            binding.spinnerListas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) return

                    val nombre = nombresConPlaceholder[position]
                    val bottomSheet = ListaComprasBottomSheetFragment("", "", nombre)
                    bottomSheet.show(parentFragmentManager, "lista_compras")
                    binding.spinnerListas.setSelection(0)
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
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
                val bottomSheet = DetalleAgenda(fechaselect, {cargarGrafico()})
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
                        val bottomSheet = ListaComprasBottomSheetFragment(fechaLimite.toString(), fechaInicio.toString(), "")
                        bottomSheet.show(parentFragmentManager, "lista_compras")
                        planear = false
                        fechaLimite = null
                        fechaInicio = null
                    }
                }
            }
        }
    }

    fun cargarGrafico() {
        lifecycleScope.launch {
            try {
                val crud = ClaseCRUD(requireContext())
                crud.iniciarBD()

                val datos = crud.obtenerCantComidasXdia()

                if (datos.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No hay datos para mostrar", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    configurarGrafico(datos)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al cargar gráfico: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun configurarGrafico(datos: List<ClaseCRUD.ComidasXdia>) {
        val entries = datos.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.cant.toFloat())
        }
        val dataSet = BarDataSet(entries, "Comidas Planificadas")
        dataSet.color = Color.parseColor("#FFDE59")
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.WHITE
        val barData = BarData(dataSet)
        barData.barWidth = 0.8f
        binding.barChart.apply {
            data = barData
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(datos.map { it.dia })
                position = XAxis.XAxisPosition.TOP
                granularity = 1f
                isGranularityEnabled = true
                textSize = 10f
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
                textSize = 10f
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = true
            setFitBars(true)
            animateY(1000)
            invalidate()
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