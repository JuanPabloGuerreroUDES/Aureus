<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Reportes"   scope="request"/>
<c:set var="pageId"    value="reportes"   scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<%-- ── PAGE HEADER ── --%>
<div class="d-flex justify-content-between align-items-start mb-4">
    <div>
        <h3 class="fw-bold mb-0">Reportes financieros</h3>
        <p class="text-muted mb-0" style="font-size:.85rem">
            Análisis de tus ingresos, gastos y balance
        </p>
    </div>
</div>

<%-- Sin cuentas --%>
<c:if test="${empty cuentas}">
    <div class="text-center py-5 text-muted">
        <i class="bi bi-bar-chart-line" style="font-size:3rem;color:#ccc"></i>
        <p class="mt-3">No tienes cuentas registradas aún.<br>Cierra sesión y vuelve a entrar para crear una automáticamente.</p>
    </div>
</c:if>

<c:if test="${not empty cuentas}">

<%-- ── FILTROS ── --%>
<div class="card border-0 shadow-sm mb-4" style="border-radius:12px">
    <div class="card-body py-3">
        <form method="get" action="/reportes" class="d-flex gap-2 align-items-center flex-wrap">
            <select name="cuentaId" class="form-select form-select-sm" style="width:auto"
                    onchange="this.form.submit()">
                <c:forEach var="c" items="${cuentas}">
                    <option value="${c.id}" ${c.id == cuentaSeleccionada ? 'selected' : ''}>
                        <c:out value="${c.name}"/>
                    </option>
                </c:forEach>
            </select>
            <input type="month" name="periodo" class="form-control form-control-sm" style="width:auto"
                   value="${periodoActual}">
            <button type="submit" class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-funnel"></i> Filtrar
            </button>
        </form>
    </div>
</div>

<%-- ── KPIs DEL MES ── --%>
<c:if test="${not empty resumen}">
<div class="row g-3 mb-4">
    <div class="col-md-3">
        <div class="kpi-card teal">
            <div class="kpi-label">Ingresos</div>
            <div class="kpi-value">+$<fmt:formatNumber value="${resumen.totalIngresos}" maxFractionDigits="0"/></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card" style="border-color:#F8C4B8">
            <div class="kpi-label">Gastos</div>
            <div class="kpi-value" style="color:#F25C3A">
                -$<fmt:formatNumber value="${resumen.totalGastos}" maxFractionDigits="0"/>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card lime">
            <div class="kpi-label">Balance</div>
            <div class="kpi-value">$<fmt:formatNumber value="${resumen.balanceNeto}" maxFractionDigits="0"/></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card">
            <div class="kpi-label">Tasa de ahorro</div>
            <div class="kpi-value" style="color:#1A9E8C">
                <fmt:formatNumber value="${resumen.tasaAhorro}" maxFractionDigits="1"/>%
            </div>
        </div>
    </div>
</div>
</c:if>

<%-- ── GRÁFICOS ── --%>
<div class="row g-4">

    <%-- Gráfico de barras: Ingresos vs Gastos últimos 6 meses --%>
    <div class="col-md-8">
        <div class="card border-0 shadow-sm h-100" style="border-radius:14px">
            <div class="card-body">
                <h6 class="fw-bold mb-3">Ingresos vs Gastos — Últimos 6 meses</h6>
                <canvas id="chartBarras" height="120"></canvas>
            </div>
        </div>
    </div>

    <%-- Gráfico de dona: Gastos por categoría --%>
    <div class="col-md-4">
        <div class="card border-0 shadow-sm h-100" style="border-radius:14px">
            <div class="card-body">
                <h6 class="fw-bold mb-3">Gastos por categoría</h6>
                <canvas id="chartDona" height="180"></canvas>
                <c:if test="${empty chartCatLabelsJson or chartCatLabelsJson == '[]'}">
                    <div class="text-center text-muted mt-3" style="font-size:.85rem">
                        <i class="bi bi-pie-chart d-block mb-1" style="font-size:2rem;opacity:.3"></i>
                        Sin gastos en este período
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <%-- Gráfico de línea: Balance neto --%>
    <div class="col-12">
        <div class="card border-0 shadow-sm" style="border-radius:14px">
            <div class="card-body">
                <h6 class="fw-bold mb-3">Evolución del balance neto</h6>
                <canvas id="chartLinea" height="60"></canvas>
            </div>
        </div>
    </div>
</div>

</c:if><%-- fin c:if cuentas --%>

<%-- ── Chart.js ── --%>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
(function() {
    const etiquetas = ${not empty chartEtiquetasJson  ? chartEtiquetasJson  : '[]'};
    const ingresos  = ${not empty chartIngresosJson   ? chartIngresosJson   : '[]'};
    const gastos    = ${not empty chartGastosJson      ? chartGastosJson     : '[]'};
    const balance   = ${not empty chartBalanceJson    ? chartBalanceJson    : '[]'};
    const catLabels = ${not empty chartCatLabelsJson  ? chartCatLabelsJson  : '[]'};
    const catVals   = ${not empty chartCatValoresJson ? chartCatValoresJson : '[]'};

    const TEAL  = '#1A9E8C';
    const CORAL = '#F25C3A';
    const LIME  = '#C8F135';
    const GOLD  = '#E8A912';
    const INK   = '#0D0F14';

    // ── Gráfico de barras agrupadas ──────────────────────────────────────
    const ctxBar = document.getElementById('chartBarras');
    if (ctxBar && etiquetas.length) {
        new Chart(ctxBar, {
            type: 'bar',
            data: {
                labels: etiquetas,
                datasets: [
                    {
                        label: 'Ingresos',
                        data: ingresos,
                        backgroundColor: TEAL,
                        borderRadius: 6,
                        borderSkipped: false
                    },
                    {
                        label: 'Gastos',
                        data: gastos,
                        backgroundColor: CORAL,
                        borderRadius: 6,
                        borderSkipped: false
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'top' },
                    tooltip: {
                        callbacks: {
                            label: ctx => ' $' + ctx.parsed.y.toLocaleString('es-CO')
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: v => '$' + Number(v).toLocaleString('es-CO')
                        }
                    }
                }
            }
        });
    }

    // ── Gráfico de dona (categorías) ─────────────────────────────────────
    const ctxDona = document.getElementById('chartDona');
    if (ctxDona && catLabels.length) {
        const palette = [TEAL, CORAL, LIME, GOLD, '#9C6ADE', '#4ECDC4', '#FF6B6B', '#45B7D1'];
        new Chart(ctxDona, {
            type: 'doughnut',
            data: {
                labels: catLabels,
                datasets: [{
                    data: catVals,
                    backgroundColor: catLabels.map((_, i) => palette[i % palette.length]),
                    borderWidth: 0,
                    hoverOffset: 8
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom', labels: { font: { size: 11 } } },
                    tooltip: {
                        callbacks: {
                            label: ctx => ' $' + ctx.parsed.toLocaleString('es-CO')
                        }
                    }
                }
            }
        });
    }

    // ── Gráfico de línea (balance neto) ──────────────────────────────────
    const ctxLine = document.getElementById('chartLinea');
    if (ctxLine && etiquetas.length) {
        new Chart(ctxLine, {
            type: 'line',
            data: {
                labels: etiquetas,
                datasets: [{
                    label: 'Balance neto',
                    data: balance,
                    borderColor: INK,
                    backgroundColor: 'rgba(200,241,53,0.15)',
                    borderWidth: 2.5,
                    fill: true,
                    tension: 0.35,
                    pointBackgroundColor: INK,
                    pointRadius: 5
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: ctx => ' $' + ctx.parsed.y.toLocaleString('es-CO')
                        }
                    }
                },
                scales: {
                    y: {
                        ticks: {
                            callback: v => '$' + Number(v).toLocaleString('es-CO')
                        }
                    }
                }
            }
        });
    }
})();
</script>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
