package com.aureus.controller;

import com.aureus.dto.report.ResumenFinancieroDto;
import com.aureus.model.Account;
import com.aureus.model.Transaction;
import com.aureus.model.User;
import com.aureus.service.TransactionService;
import com.aureus.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.util.*;

/**
 * Controlador de Reportes — implementa la sección de análisis financiero.
 *
 * NUEVO: antes este controlador no existía, causando un 404 permanente
 * cuando el usuario hacía clic en "Reportes" en el sidebar.
 *
 * Genera:
 *   - Resumen de los últimos 6 meses (ingresos, gastos, balance)
 *   - Desglose por categoría del mes seleccionado
 *   - JSON para Chart.js (renderizado en el JSP)
 */
@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReportesController {

    private final UserService        userService;
    private final TransactionService transactionService;

    @GetMapping
    public String reporte(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long cuentaId,
            @RequestParam(required = false) String periodo,
            Model model) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());
        List<Account> cuentas = userService.listarCuentas(usuario);

        model.addAttribute("cuentas", cuentas);

        if (cuentas.isEmpty()) {
            return "reportes/lista";
        }

        // Cuenta a analizar (primera por defecto)
        Account cuentaActual = (cuentaId != null)
                ? cuentas.stream().filter(c -> c.getId().equals(cuentaId)).findFirst().orElse(cuentas.get(0))
                : cuentas.get(0);

        model.addAttribute("cuentaSeleccionada", cuentaActual.getId());

        // Periodo seleccionado (mes actual por defecto)
        YearMonth ym = (periodo != null && !periodo.isBlank())
                ? YearMonth.parse(periodo)
                : YearMonth.now();
        model.addAttribute("periodoActual", ym.toString());

        // ── Últimos 6 meses para gráfico de barras ────────────────────────
        List<String>  etiquetas    = new ArrayList<>();
        List<Double>  ingresosMes  = new ArrayList<>();
        List<Double>  gastosMes    = new ArrayList<>();
        List<Double>  balanceMes   = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth mes = YearMonth.now().minusMonths(i);
            ResumenFinancieroDto r = transactionService.calcularResumen(
                    cuentaActual.getId(), mes.getMonthValue(), mes.getYear(), usuario);
            etiquetas.add(r.getPeriodoLabel());
            ingresosMes.add(r.getTotalIngresos());
            gastosMes.add(r.getTotalGastos());
            balanceMes.add(r.getBalanceNeto());
        }

        // ── Resumen del mes seleccionado ──────────────────────────────────
        ResumenFinancieroDto resumenActual = transactionService.calcularResumen(
                cuentaActual.getId(), ym.getMonthValue(), ym.getYear(), usuario);
        model.addAttribute("resumen", resumenActual);

        // ── Desglose por categoría del mes seleccionado ───────────────────
        List<Transaction> txMes = transactionService.listarPorMes(
                cuentaActual.getId(), ym.getMonthValue(), ym.getYear(), usuario);

        Map<String, Double> gastosPorCategoria = new LinkedHashMap<>();
        for (Transaction t : txMes) {
            // Solo gastos
            if ("GASTO".equals(t.getType()) || t instanceof com.aureus.model.Expense) {
                String cat = (t.getCategory() != null) ? t.getCategory().getName() : "Sin categoría";
                gastosPorCategoria.merge(cat, t.getAmount().doubleValue(), Double::sum);
            }
        }

        // Serializar a JSON para Chart.js
        try {
            ObjectMapper om = new ObjectMapper();
            model.addAttribute("chartEtiquetasJson",   om.writeValueAsString(etiquetas));
            model.addAttribute("chartIngresosJson",    om.writeValueAsString(ingresosMes));
            model.addAttribute("chartGastosJson",      om.writeValueAsString(gastosMes));
            model.addAttribute("chartBalanceJson",     om.writeValueAsString(balanceMes));
            model.addAttribute("chartCatLabelsJson",   om.writeValueAsString(new ArrayList<>(gastosPorCategoria.keySet())));
            model.addAttribute("chartCatValoresJson",  om.writeValueAsString(new ArrayList<>(gastosPorCategoria.values())));
        } catch (Exception e) {
            log.error("Error serializando datos para Chart.js", e);
        }

        return "reportes/lista";
    }
}
