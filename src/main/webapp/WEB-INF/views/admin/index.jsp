<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Panel Admin" scope="request"/>
<c:set var="pageId"    value="admin"        scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-start mb-4">
    <div>
        <h3 class="fw-bold mb-0">Panel de administración</h3>
        <p class="text-muted mb-0" style="font-size:.85rem">Acceso exclusivo para administradores</p>
    </div>
</div>

<div class="row g-3">
    <div class="col-md-4">
        <a href="/admin/usuarios" class="text-decoration-none">
            <div class="kpi-card h-100" style="cursor:pointer;transition:box-shadow .18s"
                 onmouseenter="this.style.boxShadow='0 4px 16px rgba(0,0,0,0.12)'"
                 onmouseleave="this.style.boxShadow=''">
                <div class="kpi-label">Gestión</div>
                <div class="kpi-value" style="font-size:2rem;color:#0D0F14">
                    <i class="bi bi-people"></i>
                </div>
                <div class="fw-bold mt-2">Usuarios</div>
                <div style="font-size:.8rem;color:#6E7080">Ver, activar y desactivar cuentas</div>
            </div>
        </a>
    </div>
    <div class="col-md-4">
        <div class="kpi-card h-100">
            <div class="kpi-label">Sistema</div>
            <div class="kpi-value" style="font-size:2rem;color:#1A9E8C">
                <i class="bi bi-shield-check"></i>
            </div>
            <div class="fw-bold mt-2">Seguridad</div>
            <div style="font-size:.8rem;color:#6E7080">Spring Security 6 · JWT · BCrypt(12)</div>
        </div>
    </div>
    <div class="col-md-4">
        <a href="/swagger-ui.html" target="_blank" class="text-decoration-none">
            <div class="kpi-card h-100" style="cursor:pointer;transition:box-shadow .18s"
                 onmouseenter="this.style.boxShadow='0 4px 16px rgba(0,0,0,0.12)'"
                 onmouseleave="this.style.boxShadow=''">
                <div class="kpi-label">Documentación</div>
                <div class="kpi-value" style="font-size:2rem;color:#E8A912">
                    <i class="bi bi-code-square"></i>
                </div>
                <div class="fw-bold mt-2">API Docs</div>
                <div style="font-size:.8rem;color:#6E7080">Swagger UI — OpenAPI 3</div>
            </div>
        </a>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
