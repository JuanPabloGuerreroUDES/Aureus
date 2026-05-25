<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Mis cuentas" scope="request"/>
<c:set var="pageId"    value="cuentas"      scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-start mb-4">
    <div>
        <h3 class="fw-bold mb-0">Mis cuentas</h3>
        <p class="text-muted mb-0" style="font-size:.85rem">
            Organiza tu dinero en diferentes cuentas. La cuenta principal siempre está activa.
        </p>
    </div>
    <button class="btn btn-sm" data-bs-toggle="modal" data-bs-target="#modalNuevaCuenta"
            style="background:#0D0F14;color:#C8F135;font-weight:600;border-radius:8px;padding:.5rem 1rem">
        <i class="bi bi-plus-lg"></i> Nueva cuenta
    </button>
</div>

<c:if test="${not empty successMsg}">
    <div class="alert alert-success alert-dismissible fade show">
        <i class="bi bi-check-circle-fill me-2"></i><c:out value="${successMsg}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${not empty errorMsg}">
    <div class="alert alert-danger alert-dismissible fade show">
        <i class="bi bi-exclamation-triangle-fill me-2"></i><c:out value="${errorMsg}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="row g-3">
    <c:forEach var="cuenta" items="${cuentas}" varStatus="loop">
        <c:set var="resumen" value="${resumenes[loop.index]}"/>
        <div class="col-md-6 col-lg-4">
            <div class="card border-0 shadow-sm h-100"
                 style="border-radius:16px;${cuenta.esPrincipal ? 'border:2px solid #C8F135 !important' : ''}">
                <div class="card-body p-4">

                    <%-- Header de la tarjeta --%>
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div class="d-flex align-items-center gap-2">
                            <div style="font-size:1.6rem;width:40px;height:40px;display:flex;
                                        align-items:center;justify-content:center;background:#F5F4F0;
                                        border-radius:10px">
                                <c:choose>
                                    <c:when test="${not empty cuenta.icono}">
                                        <c:out value="${cuenta.icono}"/>
                                    </c:when>
                                    <c:otherwise>🏦</c:otherwise>
                                </c:choose>
                            </div>
                            <div>
                                <div class="fw-bold" style="font-size:.95rem">
                                    <c:out value="${cuenta.name}"/>
                                </div>
                                <c:if test="${cuenta.esPrincipal}">
                                    <span class="badge" style="background:#C8F135;color:#0D0F14;
                                          font-size:.65rem;border-radius:100px;padding:.2rem .6rem">
                                        ★ Principal
                                    </span>
                                </c:if>
                            </div>
                        </div>
                        <%-- Menú de acciones --%>
                        <div class="dropdown">
                            <button class="btn btn-sm btn-link text-muted p-0" data-bs-toggle="dropdown">
                                <i class="bi bi-three-dots-vertical"></i>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end shadow-sm" style="font-size:.85rem">
                                <li>
                                    <button class="dropdown-item" onclick="abrirEditarCuenta(${cuenta.id}, '<c:out value="${cuenta.name}"/>', '<c:out value="${cuenta.icono}"/>', '<c:out value="${cuenta.descripcion}"/>')">
                                        <i class="bi bi-pencil me-2"></i>Editar
                                    </button>
                                </li>
                                <c:if test="${not cuenta.esPrincipal}">
                                    <li>
                                        <form action="/cuentas/${cuenta.id}/principal" method="post" class="d-inline">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button type="submit" class="dropdown-item">
                                                <i class="bi bi-star me-2"></i>Establecer como principal
                                            </button>
                                        </form>
                                    </li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li>
                                        <form action="/cuentas/${cuenta.id}/eliminar" method="post" class="d-inline"
                                              onsubmit="return confirm('¿Eliminar la cuenta ${cuenta.name}? Esta acción no se puede deshacer.')">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button type="submit" class="dropdown-item text-danger">
                                                <i class="bi bi-trash me-2"></i>Eliminar
                                            </button>
                                        </form>
                                    </li>
                                </c:if>
                                <c:if test="${cuenta.esPrincipal}">
                                    <li>
                                        <span class="dropdown-item text-muted" style="cursor:default">
                                            <i class="bi bi-lock me-2"></i>No se puede eliminar
                                        </span>
                                    </li>
                                </c:if>
                            </ul>
                        </div>
                    </div>

                    <%-- Descripción --%>
                    <c:if test="${not empty cuenta.descripcion}">
                        <p style="font-size:.78rem;color:#6E7080;margin-bottom:.75rem">
                            <c:out value="${cuenta.descripcion}"/>
                        </p>
                    </c:if>

                    <%-- Balance total --%>
                    <div style="background:#F5F4F0;border-radius:10px;padding:.75rem 1rem;margin-bottom:.75rem">
                        <div style="font-size:.7rem;color:#6E7080;text-transform:uppercase;letter-spacing:.06em">
                            Balance total
                        </div>
                        <div class="fw-bold" style="font-size:1.3rem;
                             color:${resumen.balanceTotal >= 0 ? '#0D0F14' : '#F25C3A'}">
                            <c:choose>
                                <c:when test="${resumen.balanceTotal >= 0}">+</c:when>
                            </c:choose>$<fmt:formatNumber value="${resumen.balanceTotal}" pattern="#,##0" maxFractionDigits="0"/>
                        </div>
                    </div>

                    <%-- KPIs del mes --%>
                    <div class="d-flex gap-2">
                        <div class="flex-fill text-center" style="background:#F0FBF9;border-radius:8px;padding:.5rem">
                            <div style="font-size:.65rem;color:#1A9E8C;text-transform:uppercase">Ingresos mes</div>
                            <div class="fw-500" style="font-size:.8rem;color:#0D0F14">
                                +$<fmt:formatNumber value="${resumen.totalIngresos}" pattern="#,##0" maxFractionDigits="0"/>
                            </div>
                        </div>
                        <div class="flex-fill text-center" style="background:#FFF4F2;border-radius:8px;padding:.5rem">
                            <div style="font-size:.65rem;color:#F25C3A;text-transform:uppercase">Gastos mes</div>
                            <div class="fw-500" style="font-size:.8rem;color:#0D0F14">
                                -$<fmt:formatNumber value="${resumen.totalGastos}" pattern="#,##0" maxFractionDigits="0"/>
                            </div>
                        </div>
                    </div>

                    <%-- Ir a transacciones de esta cuenta --%>
                    <a href="/transacciones?cuentaId=${cuenta.id}"
                       class="btn btn-sm w-100 mt-3"
                       style="background:#EDE9DF;color:#0D0F14;border-radius:8px;font-size:.8rem;font-weight:600">
                        <i class="bi bi-arrow-right-circle me-1"></i>Ver transacciones
                    </a>

                </div>
            </div>
        </div>
    </c:forEach>
</div>

<%-- ── MODAL: Nueva cuenta ── --%>
<div class="modal fade" id="modalNuevaCuenta" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content" style="border-radius:16px;border:none">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Nueva cuenta</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p style="font-size:.82rem;color:#6E7080;margin-bottom:1.2rem">
                    Añade una cuenta adicional (banco, alcancía, efectivo, etc.).
                    Tu cuenta principal siempre se mantiene independientemente.
                </p>
                <form action="/cuentas/nueva" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.82rem">Nombre *</label>
                        <input type="text" name="name" class="form-control" required maxlength="100"
                               placeholder="Ej. Alcancía, Nequi, Davivienda...">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.82rem">Icono (emoji)</label>
                        <input type="text" name="icono" class="form-control" maxlength="10"
                               placeholder="🐷  🏦  💳  💰  📦">
                        <div class="form-text">Opcional. Un emoji que represente la cuenta.</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.82rem">Descripción</label>
                        <input type="text" name="descripcion" class="form-control" maxlength="255"
                               placeholder="Ej. Para gastos del hogar">
                    </div>
                    <button type="submit" class="btn w-100 fw-bold"
                            style="background:#0D0F14;color:#C8F135;border-radius:8px">
                        <i class="bi bi-plus-lg me-1"></i>Crear cuenta
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<%-- ── MODAL: Editar cuenta ── --%>
<div class="modal fade" id="modalEditarCuenta" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content" style="border-radius:16px;border:none">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Editar cuenta</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="formEditarCuenta" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.82rem">Nombre *</label>
                        <input type="text" name="name" id="editNombre" class="form-control" required maxlength="100">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.82rem">Icono</label>
                        <input type="text" name="icono" id="editIcono" class="form-control" maxlength="10">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.82rem">Descripción</label>
                        <input type="text" name="descripcion" id="editDescripcion" class="form-control" maxlength="255">
                    </div>
                    <button type="submit" class="btn w-100 fw-bold"
                            style="background:#0D0F14;color:#C8F135;border-radius:8px">
                        <i class="bi bi-check-lg me-1"></i>Guardar cambios
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
function abrirEditarCuenta(id, nombre, icono, descripcion) {
    document.getElementById('formEditarCuenta').action = '/cuentas/' + id + '/editar';
    document.getElementById('editNombre').value      = nombre;
    document.getElementById('editIcono').value       = icono || '';
    document.getElementById('editDescripcion').value = descripcion || '';
    new bootstrap.Modal(document.getElementById('modalEditarCuenta')).show();
}
</script>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
