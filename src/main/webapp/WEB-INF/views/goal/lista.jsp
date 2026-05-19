<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Metas de ahorro" scope="request"/>
<c:set var="pageId"    value="metas"           scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-start mb-4">
    <div>
        <h3 class="fw-bold mb-0">Metas de ahorro</h3>
        <p class="text-muted mb-0" style="font-size:.85rem">
            ${metas.size()} meta(s) activa(s)
        </p>
    </div>
    <button class="btn btn-sm" data-bs-toggle="modal" data-bs-target="#modalNuevaMeta"
            style="background:#0D0F14;color:#C8F135;font-weight:600;border-radius:8px;padding:.5rem 1rem">
        <i class="bi bi-plus-lg"></i> Nueva meta
    </button>
</div>

<%-- ── TARJETAS DE METAS ── --%>
<div class="row g-3">
    <c:choose>
        <c:when test="${not empty metas}">
            <c:forEach var="meta" items="${metas}">
                <div class="col-md-4">
                    <div class="card border-0 shadow-sm h-100" style="border-radius:14px">
                        <div class="card-body">
                            <%-- Estado --%>
                            <c:if test="${meta.checkCompletion()}">
                                <span class="badge mb-2"
                                      style="background:#E0F5F2;color:#1A9E8C;border-radius:100px;font-size:.72rem">
                                    ✓ COMPLETADA
                                </span>
                            </c:if>
                            <c:if test="${meta.vencida}">
                                <span class="badge mb-2"
                                      style="background:#FEE9E4;color:#F25C3A;border-radius:100px;font-size:.72rem">
                                    ⚠ VENCIDA
                                </span>
                            </c:if>

                            <h6 class="fw-bold"><c:out value="${meta.goalName}"/></h6>
                            <div class="text-muted mb-3" style="font-size:.78rem">
                                Plazo: ${meta.deadline}
                            </div>

                            <%-- Progreso --%>
                            <div class="fw-bold mb-1" style="font-size:1.5rem">
                                <fmt:formatNumber value="${meta.calculateProgress()}" maxFractionDigits="1"/>%
                            </div>
                            <div class="progress mb-2 ${meta.calculateProgress() >= 100 ? 'progress-ok' : meta.calculateProgress() >= 50 ? 'progress-ok' : 'progress-warn'}"
                                 style="height:8px;border-radius:100px">
                                <div class="progress-bar"
                                     style="width:<fmt:formatNumber value="${meta.calculateProgress()}" maxFractionDigits="0"/>%">
                                </div>
                            </div>
                            <div class="d-flex justify-content-between" style="font-size:.75rem;color:#6E7080">
                                <span>$<fmt:formatNumber value="${meta.currentAmount}" maxFractionDigits="0"/> ahorrado</span>
                                <span>Objetivo: $<fmt:formatNumber value="${meta.targetAmount}" maxFractionDigits="0"/></span>
                            </div>

                            <%-- Acciones --%>
                            <c:if test="${!meta.checkCompletion()}">
                                <div class="mt-3 d-flex gap-2">
                                    <%-- Registrar aporte --%>
                                    <button class="btn btn-sm flex-grow-1"
                                            style="background:#E0F5F2;color:#1A9E8C;border-radius:8px;font-size:.82rem"
                                            data-bs-toggle="modal"
                                            data-bs-target="#modalAporte${meta.id}">
                                        + Registrar aporte
                                    </button>
                                    <%-- Eliminar --%>
                                    <form action="/metas/${meta.id}/eliminar" method="post"
                                          onsubmit="return confirm('¿Eliminar esta meta?')">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button type="submit" class="btn btn-sm btn-outline-danger"
                                                style="border-radius:8px">
                                            <i class="bi bi-trash3"></i>
                                        </button>
                                    </form>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <%-- Modal de aporte para esta meta --%>
                    <div class="modal fade" id="modalAporte${meta.id}" tabindex="-1">
                        <div class="modal-dialog modal-sm">
                            <div class="modal-content" style="border-radius:14px;border:none">
                                <div class="modal-header border-0">
                                    <h6 class="modal-title fw-bold">
                                        Aporte a "<c:out value="${meta.goalName}"/>"
                                    </h6>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                </div>
                                <div class="modal-body pt-0">
                                    <form action="/metas/aporte" method="post">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input type="hidden" name="goalId" value="${meta.id}">
                                        <label class="form-label" style="font-size:.82rem">Monto del aporte ($)</label>
                                        <input type="number" name="aporte" class="form-control form-control-sm mb-3"
                                               step="0.01" min="0.01" required placeholder="0.00">
                                        <button type="submit" class="btn w-100 fw-bold"
                                                style="background:#0D0F14;color:#C8F135;border-radius:8px;font-size:.9rem">
                                            Registrar aporte
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <div class="col-12">
                <div class="text-center py-5 text-muted">
                    <i class="bi bi-trophy fs-1 d-block mb-3" style="color:#C8F135"></i>
                    <h5>Sin metas de ahorro aún</h5>
                    <p style="font-size:.9rem">Crea tu primera meta y empieza a ahorrar con un objetivo claro.</p>
                    <button class="btn" data-bs-toggle="modal" data-bs-target="#modalNuevaMeta"
                            style="background:#0D0F14;color:#C8F135;border-radius:8px">
                        <i class="bi bi-plus-lg"></i> Crear primera meta
                    </button>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%-- ── MODAL: Nueva meta ── --%>
<div class="modal fade" id="modalNuevaMeta" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content" style="border-radius:16px;border:none">
            <div class="modal-header border-0">
                <h5 class="modal-title fw-bold">Nueva meta de ahorro</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form action="/metas/nueva" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                    <div class="mb-3">
                        <label class="form-label" style="font-size:.82rem">Nombre de la meta</label>
                        <input type="text" name="goalName" class="form-control"
                               placeholder="Ej. Fondo de emergencia" required maxlength="150">
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Monto objetivo ($)</label>
                            <input type="number" name="targetAmount" class="form-control"
                                   step="0.01" min="1" required placeholder="0.00">
                        </div>
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Fecha límite</label>
                            <input type="date" name="deadline" class="form-control" required>
                        </div>
                    </div>
                    <button type="submit" class="btn w-100 fw-bold"
                            style="background:#0D0F14;color:#C8F135;border-radius:8px">
                        Crear meta
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
