<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Transacciones" scope="request"/>
<c:set var="pageId"    value="transacciones"  scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<%-- ── PAGE HEADER ── --%>
<div class="d-flex justify-content-between align-items-start mb-4">
    <div>
        <h3 class="fw-bold mb-0">Transacciones</h3>
        <p class="text-muted mb-0" style="font-size:.85rem">
            Registro de ingresos y gastos
            <c:if test="${not empty resumen}"> · <c:out value="${resumen.periodoLabel}"/></c:if>
        </p>
    </div>
    <button class="btn btn-sm" data-bs-toggle="modal" data-bs-target="#modalNueva"
            style="background:#0D0F14;color:#C8F135;font-weight:600;border-radius:8px;padding:.5rem 1rem">
        <i class="bi bi-plus-lg"></i> Nueva transacción
    </button>
</div>

<%-- ── RESUMEN ── --%>
<c:if test="${not empty resumen}">
<div class="row g-3 mb-4">
    <div class="col-md-4">
        <div class="kpi-card teal">
            <div class="kpi-label">Ingresos</div>
            <div class="kpi-value">+$<fmt:formatNumber value="${resumen.totalIngresos}" maxFractionDigits="0"/></div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="kpi-card" style="border-color:#F8C4B8">
            <div class="kpi-label">Gastos</div>
            <div class="kpi-value" style="color:#F25C3A">
                -$<fmt:formatNumber value="${resumen.totalGastos}" maxFractionDigits="0"/>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="kpi-card lime">
            <div class="kpi-label">Balance neto</div>
            <div class="kpi-value">$<fmt:formatNumber value="${resumen.balanceNeto}" maxFractionDigits="0"/></div>
        </div>
    </div>
</div>
</c:if>

<%-- ── FILTROS ── --%>
<div class="card border-0 shadow-sm mb-3" style="border-radius:12px">
    <div class="card-body py-3">
        <form method="get" action="/transacciones" class="d-flex gap-2 align-items-center flex-wrap">
            <select name="cuentaId" class="form-select form-select-sm" style="width:auto">
                <option value="">Selecciona cuenta</option>
                <c:forEach var="c" items="${cuentas}">
                    <option value="${c.id}" ${c.id == cuentaSeleccionada ? 'selected' : ''}>
                        <c:out value="${c.name}"/>
                    </option>
                </c:forEach>
            </select>
            <input type="month" name="periodo" class="form-control form-control-sm" style="width:auto">
            <button type="submit" class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-funnel"></i> Filtrar
            </button>
        </form>
    </div>
</div>

<%-- ── TABLA DE TRANSACCIONES ── --%>
<div class="card border-0 shadow-sm" style="border-radius:12px">
    <div class="card-body p-0">
        <table class="table table-aureus table-hover mb-0">
            <thead>
                <tr>
                    <th class="ps-3">Fecha</th>
                    <th>Descripción</th>
                    <th>Categoría</th>
                    <th>Tipo</th>
                    <th class="text-end">Monto</th>
                    <th class="text-center pe-3">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty transacciones}">
                        <c:forEach var="t" items="${transacciones}">
                            <tr>
                                <td class="ps-3" style="color:#6E7080;font-size:.8rem">
                                    ${t.date}
                                </td>
                                <td>
                                    <div class="fw-500"><c:out value="${t.description}"/></div>
                                    <c:if test="${t.recurring}">
                                        <span class="badge badge-fixed" style="font-size:.68rem;border-radius:100px">
                                            ↻ Recurrente
                                        </span>
                                    </c:if>
                                </td>
                                <td>
                                    <span class="badge ${t.type == 'INGRESO' ? 'badge-income' : 'badge-expense'}"
                                          style="border-radius:100px;font-size:.72rem">
                                        <c:out value="${t.category != null ? t.category.name : '—'}"/>
                                    </span>
                                </td>
                                <td>
                                    <span class="badge ${t.type == 'INGRESO' ? 'badge-income' : 'badge-expense'}"
                                          style="border-radius:100px;font-size:.7rem">
                                        <c:out value="${t.type}"/>
                                    </span>
                                </td>
                                <td class="text-end fw-500
                                    ${t.type == 'INGRESO' ? 'text-success' : 'text-danger'}">
                                    ${t.type == 'INGRESO' ? '+' : '-'}$<fmt:formatNumber
                                        value="${t.amount}" maxFractionDigits="2"/>
                                </td>
                                <td class="text-center pe-3">
                                    <%-- Eliminar: POST con token CSRF --%>
                                    <form action="/transacciones/${t.id}/eliminar"
                                          method="post" class="d-inline"
                                          onsubmit="return confirm('¿Eliminar esta transacción?')">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input type="hidden" name="cuentaId" value="${cuentaSeleccionada}">
                                        <button type="submit" class="btn btn-sm btn-link text-danger p-0"
                                                title="Eliminar">
                                            <i class="bi bi-trash3"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" class="text-center text-muted py-5">
                                <i class="bi bi-inbox fs-3 d-block mb-2"></i>
                                <c:choose>
                                    <c:when test="${empty cuentaSeleccionada}">
                                        <c:choose><c:when test="${empty cuentas}">No tienes cuentas creadas. Cierra sesión, vuelve a entrar y se creará automáticamente.</c:when><c:otherwise>Selecciona una cuenta arriba para ver sus transacciones.</c:otherwise></c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        No hay transacciones en este período.
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
                <%--
                    Token CSRF incluido en este formulario.
                    Spring Security rechazará el POST si falta (Sección 6.1).
                --%>
                <form action="/transacciones/nueva" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                    <%-- Tipo de transacción --%>
                    <div class="mb-3">
                        <label class="form-label fw-600" style="font-size:.82rem">Tipo</label>
                        <div class="d-flex gap-2">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="tipo"
                                       id="tipoGasto" value="GASTO" checked>
                                <label class="form-check-label" for="tipoGasto">Gasto</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="tipo"
                                       id="tipoIngreso" value="INGRESO">
                                <label class="form-check-label" for="tipoIngreso">Ingreso</label>
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
                            <input type="date" name="date" class="form-control form-control-sm" required>
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
                                    <option value="${cat.id}">
                                        <c:out value="${cat.icono}"/> <c:out value="${cat.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Cuenta</label>
                            <select name="accountId" class="form-select form-select-sm" required>
                                <option value="" disabled selected>— Seleccionar cuenta —</option>
                                <c:forEach var="cuenta" items="${cuentas}">
                                    <option value="${cuenta.id}">
                                        <c:out value="${cuenta.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="mb-3 form-check">
                        <input type="checkbox" class="form-check-input" name="isRecurring" id="chkRecurring">
                        <label class="form-check-label" for="chkRecurring" style="font-size:.85rem">
                            Transacción recurrente
                        </label>
                    </div>

                    <button type="submit" class="btn w-100 fw-bold"
                            style="background:#0D0F14;color:#C8F135;border-radius:8px">
                        Registrar transacción
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
