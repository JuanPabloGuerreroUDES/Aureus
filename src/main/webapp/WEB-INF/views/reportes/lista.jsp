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

<%-- ── BALANCE TOTAL HISTÓRICO ── --%>
<c:if test="${not empty resumenTotal}">
<div class="card border-0 mb-4"
     style="background:#0D0F14;border-radius:16px;padding:1.4rem 1.8rem">
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
        <div>
            <div style="font-size:.7rem;color:rgba(255,255,255,.4);text-transform:uppercase;letter-spacing:.08em;margin-bottom:.3rem">
                Balance total acumulado de la cuenta
            </div>
            <div style="font-size:2.2rem;font-weight:800;color:#C8F135;line-height:1">
                $<fmt:formatNumber value="${resumenTotal.balanceTotal}" pattern="#,##0" maxFractionDigits="0"/>
            </div>
        </div>
        <div class="d-flex gap-4">
            <div>
                <div style="font-size:.68rem;color:rgba(255,255,255,.4);text-transform:uppercase">Total ingresos</div>
                <div style="font-weight:700;color:#4ECDC4">
                    +$<fmt:formatNumber value="${resumenTotal.totalIngresosHistorico}" pattern="#,##0" maxFractionDigits="0"/>
                </div>
            </div>
            <div>
                <div style="font-size:.68rem;color:rgba(255,255,255,.4);text-transform:uppercase">Total gastos</div>
                <div style="font-weight:700;color:#F25C3A">
                    -$<fmt:formatNumber value="${resumenTotal.totalGastosHistorico}" pattern="#,##0" maxFractionDigits="0"/>
                </div>
            </div>
        </div>
    </div>
</div>
</c:if>

<%-- ── KPIs DEL MES SELECCIONADO ── --%>
<c:if test="${not empty resumen}">
<div class="d-flex align-items-center gap-2 mb-3">
    <div style="width:3px;height:14px;background:#C8F135;border-radius:2px"></div>
    <span style="font-size:.78rem;color:#6E7080;font-weight:600;text-transform:uppercase;letter-spacing:.06em">
        Período · <c:out value="${resumen.periodoLabel}"/>
    </span>
</div>
<div class="row g-3 mb-4">
    <div class="col-md-3">
        <div class="kpi-card teal">
            <div class="kpi-label">Ingresos</div>
            <div class="kpi-value">+$<fmt:formatNumber value="${resumen.totalIngresos}" pattern="#,##0" maxFractionDigits="0"/></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card" style="border-color:#F8C4B8">
            <div class="kpi-label">Gastos</div>
            <div class="kpi-value" style="color:#F25C3A">
                -$<fmt:formatNumber value="${resumen.totalGastos}" pattern="#,##0" maxFractionDigits="0"/>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card lime">
            <div class="kpi-label">Balance del período</div>
            <div class="kpi-value" style="color:${resumen.balanceNeto >= 0 ? '#0D0F14' : '#F25C3A'}">
                ${resumen.balanceNeto >= 0 ? '+' : ''}<fmt:formatNumber value="${resumen.balanceNeto}" pattern="#,##0" maxFractionDigits="0"/>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card">
            <div class="kpi-label">Tasa de ahorro</div>
            <div class="kpi-value" style="color:#1A9E8C">
                <c:choose>
                    <c:when test="${resumen.totalIngresos > 0}">
                        <fmt:formatNumber value="${resumen.tasaAhorro}" maxFractionDigits="1"/>%
                    </c:when>
                    <c:otherwise>—</c:otherwise>
                </c:choose>
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
