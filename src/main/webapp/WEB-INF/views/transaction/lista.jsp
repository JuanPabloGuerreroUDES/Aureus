<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Transacciones" scope="request"/>
<c:set var="pageId"    value="transacciones"  scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-start mb-4">
    <div>
        <h3 class="fw-bold mb-0">Transacciones</h3>
        <p class="text-muted mb-0" style="font-size:.85rem">
            <c:choose>
                <c:when test="${modoFiltrado}">
                    Mostrando · <strong><c:out value="${resumen.periodoLabel}"/></strong>
                </c:when>
                <c:otherwise>Historial completo de la cuenta</c:otherwise>
            </c:choose>
        </p>
    </div>
    <button class="btn btn-sm" data-bs-toggle="modal" data-bs-target="#modalNueva"
            style="background:#0D0F14;color:#C8F135;font-weight:600;border-radius:8px;padding:.5rem 1rem">
        <i class="bi bi-plus-lg"></i> Nueva transacción
    </button>
</div>

<%-- ── KPIs ── --%>
<div class="row g-3 mb-4">
    <div class="col-md-4">
        <div class="card border-0 shadow-sm" style="border-radius:14px;background:#0D0F14;padding:1.2rem 1.4rem">
            <div style="font-size:.68rem;color:rgba(255,255,255,.4);text-transform:uppercase;letter-spacing:.06em">Balance total</div>
            <div class="fw-bold" style="font-size:1.5rem;color:#C8F135">
                $<fmt:formatNumber value="${resumen != null ? resumen.balanceTotal : 0}" pattern="#,##0" maxFractionDigits="0"/>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="kpi-card teal">
            <div class="kpi-label">${modoFiltrado ? 'Ingresos del período' : 'Ingresos del mes'}</div>
            <div class="kpi-value">+$<fmt:formatNumber value="${resumen != null ? resumen.totalIngresos : 0}" pattern="#,##0" maxFractionDigits="0"/></div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="kpi-card" style="border-color:#F8C4B8">
            <div class="kpi-label">${modoFiltrado ? 'Gastos del período' : 'Gastos del mes'}</div>
            <div class="kpi-value" style="color:#F25C3A">-$<fmt:formatNumber value="${resumen != null ? resumen.totalGastos : 0}" pattern="#,##0" maxFractionDigits="0"/></div>
        </div>
    </div>
</div>

<%-- ── FILTROS ── --%>
<div class="card border-0 shadow-sm mb-3" style="border-radius:12px">
    <div class="card-body py-3">
        <form method="get" action="/transacciones" class="d-flex gap-2 align-items-center flex-wrap">
            <%-- Selector de cuenta --%>
            <select name="cuentaId" class="form-select form-select-sm" style="width:auto"
                    onchange="this.form.submit()">
                <c:forEach var="c" items="${cuentas}">
                    <option value="${c.id}" ${c.id == cuentaSeleccionada ? 'selected' : ''}>
                        <c:out value="${not empty c.icono ? c.icono : '🏦'}"/> <c:out value="${c.name}"/>
                        ${c.esPrincipal ? '★' : ''}
                    </option>
                </c:forEach>
            </select>

            <%-- Filtro de mes (opcional) --%>
            <input type="month" name="periodo" class="form-control form-control-sm" style="width:auto"
                   value="${periodoActual}" title="Filtrar por mes (opcional)">
            <button type="submit" class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-funnel"></i> Filtrar mes
            </button>

            <c:if test="${modoFiltrado}">
                <a href="/transacciones?cuentaId=${cuentaSeleccionada}"
                   class="btn btn-sm btn-link text-muted p-0">
                    <i class="bi bi-x-circle me-1"></i>Ver todo
                </a>
            </c:if>
        </form>
    </div>
</div>

<%-- ── TABLA ── --%>
<div class="card border-0 shadow-sm" style="border-radius:12px">
    <div class="card-body p-0">
        <table class="table table-aureus table-hover mb-0">
            <thead>
                <tr>
                    <th class="ps-3">Fecha</th>
                    <th>Descripción</th>
                    <th>Categoría</th>
                    <th class="text-end">Monto</th>
                    <th class="text-center pe-3">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty transacciones}">
                        <c:forEach var="t" items="${transacciones}">
                            <tr>
                                <td class="ps-3" style="color:#6E7080;font-size:.8rem;white-space:nowrap">${t.date}</td>
                                <td>
                                    <div class="fw-500" style="font-size:.875rem">
                                        <c:out value="${not empty t.description ? t.description : t.type}"/>
                                    </div>
                                </td>
                                <td>
                                    <span class="badge ${t.type == 'INGRESO' ? 'badge-income' : 'badge-expense'}"
                                          style="border-radius:100px;font-size:.72rem">
                                        <c:out value="${t.category != null ? t.category.name : '—'}"/>
                                    </span>
                                </td>
                                <td class="text-end fw-500 ${t.type == 'INGRESO' ? 'text-success' : 'text-danger'}">
                                    ${t.type == 'INGRESO' ? '+' : '-'}$<fmt:formatNumber value="${t.amount}" maxFractionDigits="2"/>
                                </td>
                                <td class="text-center pe-3">
                                    <form action="/transacciones/${t.id}/eliminar" method="post" class="d-inline"
                                          onsubmit="return confirm('¿Eliminar esta transacción?')">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input type="hidden" name="cuentaId" value="${cuentaSeleccionada}">
                                        <button type="submit" class="btn btn-sm btn-link text-danger p-0" title="Eliminar">
                                            <i class="bi bi-trash3"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="5" class="text-center text-muted py-5">
                                <i class="bi bi-inbox fs-3 d-block mb-2"></i>
                                <c:choose>
                                    <c:when test="${modoFiltrado}">
                                        No hay transacciones en este período.
                                        <a href="/transacciones?cuentaId=${cuentaSeleccionada}" style="color:#1A9E8C">Ver todo →</a>
                                    </c:when>
                                    <c:otherwise>
                                        Aún no hay transacciones en esta cuenta.
                                        <a href="#" data-bs-toggle="modal" data-bs-target="#modalNueva" style="color:#1A9E8C">
                                            Agrega la primera →
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<%-- ── MODAL: Nueva transacción ── --%>
<div class="modal fade" id="modalNueva" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content" style="border-radius:16px;border:none">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Nueva transacción</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form action="/transacciones/nueva" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="mb-3">
                        <div class="d-flex gap-3">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="tipo" id="tipoGasto" value="GASTO" checked>
                                <label class="form-check-label" for="tipoGasto">
                                    <i class="bi bi-arrow-up-right text-danger me-1"></i>Gasto
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="tipo" id="tipoIngreso" value="INGRESO">
                                <label class="form-check-label" for="tipoIngreso">
                                    <i class="bi bi-arrow-down-left text-success me-1"></i>Ingreso
                                </label>
                            </div>
                        </div>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Monto ($)</label>
                            <input type="number" name="amount" class="form-control form-control-sm"
                                   step="0.01" min="0.01" required placeholder="0.00">
                        </div>
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Fecha</label>
                            <input type="date" name="date" class="form-control form-control-sm"
                                   id="fechaTx" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" style="font-size:.82rem">Descripción</label>
                        <input type="text" name="description" class="form-control form-control-sm"
                               placeholder="Ej. Supermercado Éxito" maxlength="255">
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Categoría</label>
                            <select name="categoryId" class="form-select form-select-sm">
                                <option value="">Sin categoría</option>
                                <c:forEach var="cat" items="${categorias}">
                                    <option value="${cat.id}"><c:out value="${cat.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Cuenta</label>
                            <select name="accountId" class="form-select form-select-sm" required>
                                <c:forEach var="cuenta" items="${cuentas}">
                                    <option value="${cuenta.id}" ${cuenta.id == cuentaSeleccionada ? 'selected' : ''}>
                                        <c:out value="${cuenta.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <button type="submit" class="btn w-100 fw-bold"
                            style="background:#0D0F14;color:#C8F135;border-radius:8px">
                        <i class="bi bi-check-lg me-1"></i>Registrar transacción
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
<script>
document.addEventListener('DOMContentLoaded', function() {
    const f = document.getElementById('fechaTx');
    if (f && !f.value) f.value = new Date().toISOString().split('T')[0];
});
</script>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
