package com.example.smartmealsproyecto

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.smartmealsproyecto.databinding.FragmentGraficasBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Graficas : Fragment() {
    private var _binding: FragmentGraficasBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = Graficas()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraficasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ClaseUsuario.iduser == 0) {
            Toast.makeText(context, "Inicia sesión", Toast.LENGTH_SHORT).show()
            return
        }

        cargarGrafico()
    }

    private fun cargarGrafico() {
        lifecycleScope.launch {
            try {
                val crud = ClaseCRUD(requireContext())
                crud.iniciarBD()

                val datos = crud.obtenerIngredientesMasUsados(ClaseUsuario.iduser)

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

    private fun configurarGrafico(datos: List<IngredienteEstadistica>) {
        // Preparar entradas para el gráfico
        val entries = datos.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.usos.toFloat())
        }

        // Configurar dataset
        val dataSet = BarDataSet(entries, "Veces usado")
        dataSet.color = Color.parseColor("#FF6F00")
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        // Crear BarData
        val barData = BarData(dataSet)
        barData.barWidth = 0.8f

        // Configurar el gráfico
        binding.barChart.apply {
            data = barData

            // Configurar eje X
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(datos.map { it.nombre })
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                textSize = 10f
                setDrawGridLines(false)
            }

            // Configurar eje Y izquierdo
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
                textSize = 10f
            }

            // Deshabilitar eje Y derecho
            axisRight.isEnabled = false

            // Configuración general
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
}
