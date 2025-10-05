package com.example.smartmealsproyecto

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
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
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el botón para abrir el BottomSheet
        binding.buttonMostrarListaCompras.setOnClickListener {
            val bottomSheet = ListaComprasBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, "lista_compras")
        }

        // Configurar botones de navegación del calendario
        binding.btnAnterior.setOnClickListener {
            mesAnterior()
        }

        binding.btnSiguiente.setOnClickListener {
            mesSiguiente()
        }

        // Inicializar el calendario
        actualizarCalendario()
    }

    private fun actualizarCalendario() {
        binding.gridCalendario.removeAllViews()

        // Obtener información del mes actual
        val mes = calendario.get(Calendar.MONTH)
        val anio = calendario.get(Calendar.YEAR)

        // Establecer el título del mes
        val nombresMeses = arrayOf(
            "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
            "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"
        )
        binding.tvMes.text = "${nombresMeses[mes]} $anio"

        // Obtener el primer día del mes
        val primerDia = calendario.clone() as Calendar
        primerDia.set(Calendar.DAY_OF_MONTH, 1)
        val diaSemana = primerDia.get(Calendar.DAY_OF_WEEK) - 1 // 0=Domingo

        // Obtener el número de días del mes
        val diasEnMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Crear celdas vacías antes del primer día
        repeat(diaSemana) {
            agregarCelda("", false)
        }

        // Crear celdas con los días del mes
        val diaActual = calendario.get(Calendar.DAY_OF_MONTH)
        val mesActual = Calendar.getInstance().get(Calendar.MONTH)
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        val esMesActual = (mes == mesActual && anio == anioActual)

        for (dia in 1..diasEnMes) {
            agregarCelda(dia.toString(), esMesActual && dia == diaActual)
        }
    }

    private fun agregarCelda(texto: String, esHoy: Boolean) {
        val celda = TextView(requireContext()).apply {
            // Configurar parámetros de layout
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                setMargins(2, 2, 2, 2)
            }

            this.text = texto
            gravity = Gravity.CENTER
            setPadding(16, 24, 16, 24)
            textSize = 16f

            // Estilo de la celda
            when {
                esHoy -> {
                    setBackgroundColor(Color.parseColor("#FF6B35"))
                    setTextColor(Color.WHITE)
                    textSize = 18f
                }
                texto.isNotEmpty() -> {
                    setBackgroundColor(Color.parseColor("#FFE8D6"))
                    setTextColor(Color.parseColor("#333333"))
                }
                else -> {
                    setBackgroundColor(Color.TRANSPARENT)
                }
            }

            // Click listener para seleccionar día
            setOnClickListener {
                if (texto.isNotEmpty()) {
                    onDiaSeleccionado(texto.toInt())
                }
            }
        }

        binding.gridCalendario.addView(celda)
    }

    private fun mesAnterior() {
        calendario.add(Calendar.MONTH, -1)
        actualizarCalendario()
    }

    private fun mesSiguiente() {
        calendario.add(Calendar.MONTH, 1)
        actualizarCalendario()
    }

    private fun onDiaSeleccionado(dia: Int) {
        // Aquí puedes manejar la selección de un día
        // Por ejemplo, mostrar comidas planificadas para ese día
        val mes = calendario.get(Calendar.MONTH) + 1
        val anio = calendario.get(Calendar.YEAR)
        // TODO: Implementar lógica cuando se selecciona un día
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = Calendario()
    }
}