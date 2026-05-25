<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Usuarios — Admin" scope="request"/>
<c:set var="pageId"    value="admin"             scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h3 class="fw-bold mb-0">Gestión de usuarios</h3>
        <p class="text-muted mb-0" style="font-size:.85rem">Solo administradores</p>
    </div>
    <a href="/admin" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> Volver al panel
    </a>
</div>

<div class="alert alert-info">
    <i class="bi bi-info-circle me-2"></i>
    Esta sección está en construcción. Los usuarios se gestionan directamente desde la base de datos o a través de la API REST en <a href="/swagger-ui.html" class="alert-link">Swagger UI</a>.
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
