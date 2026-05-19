<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Presupuestos" scope="request"/>
<c:set var="pageId"    value="presupuestos"  scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h3 class="fw-bold mb-0" style="color:#0D0F14">Presupuestos</h3>
        <p class="text-muted mb-0" style="font-size:.85rem">Controla tus límites de gasto por categoría</p>
    </div>
    <button class="btn btn-sm" data-bs-toggle="modal" data-bs-target="#modalNuevoPresupuesto"
            style="background:#0D0F14;color:#C8F135;font-weight:600;border-radius:8px;padding:.5rem 1rem">
        <i class="bi bi-plus-lg"></i> Nuevo presupuesto
    </button>
</div>

<%-- Mensajes flash --%>
<c:if test="${not empty successMsg}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i><c:out value="${successMsg}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${not empty errorMsg}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i><c:out value="${errorMsg}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<%-- Lista de presupuestos --%>
<c:choose>
    <c:when test="${empty presupuestos}">
        <div class="text-center py-5">
            <i class="bi bi-pie-chart" style="font-size:3rem;color:#ccc"></i>
            <p class="text-muted mt-3">No tienes presupuestos activos.<br>Crea uno para controlar tus gastos.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row g-3">
            <c:forEach var="p" items="${presupuestos}">
                <div class="col-md-6 col-lg-4">
                    <div class="card border-0 shadow-sm h-100" style="border-radius:16px">
                        <div class="card-body p-4">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <span class="badge mb-2" style="background:#EDE9DF;color:#0D0F14;font-size:.75rem">
                                        <c:out value="${p.category.name}"/>
                                    </span>
                                    <div class="fw-bold">
                                        Límite: $<fmt:formatNumber value="${p.limitAmount}" pattern="#,##0.00"/>
                                    </div>
                                </div>
                                <form action="${pageContext.request.contextPath}/presupuestos/${p.id}/eliminar" method="post" style="display:inline">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <input type="hidden" name="cuentaId" value="${p.account.id}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger border-0">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </form>
                            </div>
                            <div class="text-muted" style="font-size:.8rem">
                                <i class="bi bi-calendar3 me-1"></i>
                                ${p.startDate} —
                                ${p.endDate}
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%-- Modal nuevo presupuesto --%>
<div class="modal fade" id="modalNuevoPresupuesto" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content" style="border-radius:16px">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Nuevo presupuesto</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form action="${pageContext.request.contextPath}/presupuestos/nuevo" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.85rem">Cuenta</label>
                        <select name="accountId" class="form-select" required>
                            <c:forEach var="cuenta" items="${cuentas}">
                                <option value="${cuenta.id}"><c:out value="${cuenta.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.85rem">Categoría</label>
                        <select name="categoryId" class="form-select" required>
                            <c:forEach var="cat" items="${categorias}">
                                <option value="${cat.id}"><c:out value="${cat.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.85rem">Límite ($)</label>
                        <input type="number" name="limitAmount" class="form-control" step="0.01" min="0.01" required>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col">
                            <label class="form-label fw-semibold" style="font-size:.85rem">Desde</label>
                            <input type="date" name="startDate" class="form-control" required>
                        </div>
                        <div class="col">
                            <label class="form-label fw-semibold" style="font-size:.85rem">Hasta</label>
                            <input type="date" name="endDate" class="form-control" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold" style="font-size:.85rem">Alerta al (%)</label>
                        <input type="number" name="umbralAlerta" class="form-control" value="80" min="1" max="100">
                    </div>
                    <div class="d-grid">
                        <button type="submit" class="btn fw-bold"
                                style="background:#0D0F14;color:#C8F135;border-radius:8px">
                            Crear presupuesto
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
