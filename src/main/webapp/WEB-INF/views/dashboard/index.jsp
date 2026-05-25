<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<c:set var="pageId"    value="dashboard"  scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<%-- ── PAGE HEADER ── --%>
<div class="d-flex justify-content-between align-items-start mb-4">
    <div>
        <h3 class="fw-bold mb-0" style="color:#0D0F14">
            Buenos días, <c:out value="${usuario.name}"/> 👋
        </h3>
        <p class="text-muted mb-0" style="font-size:.85rem">
            <c:out value="${resumen != null ? resumen.periodoLabel : ''}"/> · Tu balance va bien este mes
        </p>
    </div>
    <%-- FIX: antes apuntaba a GET /transacciones/nueva que no existe.
         Ahora abre el modal de nueva transacción directamente. --%>
    <button class="btn btn-sm" data-bs-toggle="modal" data-bs-target="#modalNuevaQuick"
            style="background:#0D0F14;color:#C8F135;font-weight:600;border-radius:8px;padding:.5rem 1rem">
        <i class="bi bi-plus-lg"></i> Agregar
    </button>
</div>

<%-- ── KPI ROW ── --%>
<div class="row g-3 mb-4">
    <div class="col-md-3">
        <div class="kpi-card lime">
            <div class="kpi-label">Balance actual</div>
            <div class="kpi-value">
                $<fmt:formatNumber value="${resumen != null ? resumen.balanceNeto : 0}" maxFractionDigits="0"/>
            </div>
            <div class="mt-2" style="font-size:.75rem">↑ Actualizado ahora</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card teal">
            <div class="kpi-label">Ingresos del mes</div>
            <div class="kpi-value">
                $<fmt:formatNumber value="${resumen != null ? resumen.totalIngresos : 0}" maxFractionDigits="0"/>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card">
            <div class="kpi-label">Gastos del mes</div>
            <div class="kpi-value" style="color:#F25C3A">
                $<fmt:formatNumber value="${resumen != null ? resumen.totalGastos : 0}" maxFractionDigits="0"/>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card">
            <div class="kpi-label">Tasa de ahorro</div>
            <div class="kpi-value" style="color:#1A9E8C">
                <fmt:formatNumber value="${resumen != null ? resumen.tasaAhorro : 0}" maxFractionDigits="1"/>%
            </div>
        </div>
    </div>
</div>

<%-- ── RECENT TRANSACTIONS + GOALS ── --%>
<div class="row g-3">

    <%-- Últimas transacciones --%>
    <div class="col-md-7">
        <div class="card border-0 shadow-sm" style="border-radius:12px">
            <div class="card-body p-0">
                <div class="d-flex justify-content-between align-items-center p-3 pb-0">
                    <h6 class="fw-bold mb-0">Últimas transacciones</h6>
                    <a href="/transacciones" style="font-size:.8rem;color:#1A9E8C;text-decoration:none">
                        Ver todas →
                    </a>
                </div>
                <table class="table table-aureus table-hover mb-0">
                    <thead>
                        <tr>
                            <th class="ps-3">Descripción</th>
                            <th>Categoría</th>
                            <th class="text-end pe-3">Monto</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty ultimasTransacciones}">
                                <c:forEach var="t" items="${ultimasTransacciones}">
                                    <tr>
                                        <td class="ps-3">
                                            <div class="fw-500" style="font-size:.875rem">
                                                <c:out value="${t.description != null ? t.description : t.type}"/>
                                            </div>
                                            <div style="font-size:.72rem;color:#6E7080">
                                                ${t.date}
                                            </div>
                                        </td>
                                        <td>
                                            <span class="badge ${t.type == 'INGRESO' ? 'badge-income' : 'badge-expense'}"
                                                  style="font-size:.72rem;padding:.3rem .7rem;border-radius:100px">
                                                <c:out value="${t.category != null ? t.category.name : t.type}"/>
                                            </span>
                                        </td>
                                        <td class="text-end pe-3">
                                            <span class="${t.type == 'INGRESO' ? 'text-success' : 'text-danger'} fw-500">
                                                ${t.type == 'INGRESO' ? '+' : '-'}$<fmt:formatNumber value="${t.amount}" maxFractionDigits="0"/>
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="3" class="text-center text-muted py-4" style="font-size:.85rem">
                                        <i class="bi bi-inbox fs-4 d-block mb-2"></i>
                                        Sin transacciones este mes.
                                        <a href="/transacciones" style="color:#1A9E8C">Agrega la primera →</a>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Metas de ahorro --%>
    <div class="col-md-5">
        <div class="card border-0 shadow-sm" style="border-radius:12px">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h6 class="fw-bold mb-0">Metas activas</h6>
                    <a href="/metas" style="font-size:.8rem;color:#1A9E8C;text-decoration:none">
                        Ver todas →
                    </a>
                </div>

                <c:choose>
                    <c:when test="${not empty metas}">
                        <c:forEach var="meta" items="${metas}" end="2">
                            <div class="mb-4">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <div>
                                        <div class="fw-500" style="font-size:.875rem">
                                            <c:out value="${meta.goalName}"/>
                                        </div>
                                        <div style="font-size:.72rem;color:#6E7080">
                                            Meta: ${meta.deadline}
                                        </div>
                                    </div>
                                    <div class="fw-bold" style="font-size:1.1rem">
                                        <fmt:formatNumber value="${meta.calculateProgress()}" maxFractionDigits="0"/>%
                                    </div>
                                </div>
                                <div class="progress ${meta.calculateProgress() >= 80 ? 'progress-ok' : 'progress-warn'}"
                                     style="height:6px;border-radius:100px">
                                    <div class="progress-bar"
                                         style="width:<fmt:formatNumber value="${meta.calculateProgress()}" maxFractionDigits="0"/>%">
                                    </div>
                                </div>
                                <div class="d-flex justify-content-between mt-1" style="font-size:.72rem;color:#6E7080">
                                    <span>$<fmt:formatNumber value="${meta.currentAmount}" maxFractionDigits="0"/> ahorrado</span>
                                    <span>Objetivo: $<fmt:formatNumber value="${meta.targetAmount}" maxFractionDigits="0"/></span>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center text-muted py-4" style="font-size:.85rem">
                            <i class="bi bi-trophy fs-4 d-block mb-2"></i>
                            Sin metas creadas aún.
                            <a href="/metas" style="color:#1A9E8C">Crea tu primera meta →</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%-- ── MODAL: transacción rápida desde el dashboard ── --%>
<div class="modal fade" id="modalNuevaQuick" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content" style="border-radius:16px;border:none">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Registrar transacción</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form action="/transacciones/nueva" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="mb-3">
                        <div class="d-flex gap-3">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="tipo" value="GASTO" checked>
                                <label class="form-check-label"><i class="bi bi-arrow-up-right text-danger me-1"></i>Gasto</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="tipo" value="INGRESO">
                                <label class="form-check-label"><i class="bi bi-arrow-down-left text-success me-1"></i>Ingreso</label>
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
                                   id="fechaQuick" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" style="font-size:.82rem">Descripción</label>
                        <input type="text" name="description" class="form-control form-control-sm"
                               placeholder="Ej. Supermercado" maxlength="255">
                    </div>
                    <div class="mb-3">
                        <label class="form-label" style="font-size:.82rem">Cuenta</label>
                        <select name="accountId" class="form-select form-select-sm" required>
                            <option value="" disabled selected>— Seleccionar —</option>
                            <c:forEach var="cuenta" items="${cuentas}">
                                <option value="${cuenta.id}"><c:out value="${cuenta.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn w-100 fw-bold"
                            style="background:#0D0F14;color:#C8F135;border-radius:8px">
                        <i class="bi bi-check-lg me-1"></i>Registrar
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
<script>
document.addEventListener('DOMContentLoaded', function() {
    const f = document.getElementById('fechaQuick');
    if (f) f.value = new Date().toISOString().split('T')[0];
});
</script>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
