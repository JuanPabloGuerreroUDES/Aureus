<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="pageTitle" value="Mi perfil" scope="request"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="mb-4">
    <h3 class="fw-bold mb-0">Mi perfil</h3>
    <p class="text-muted mb-0" style="font-size:.85rem">Gestiona tu información y contraseña</p>
</div>

<div class="row g-4">

    <%-- ── Info del usuario ── --%>
    <div class="col-md-4">
        <div class="card border-0 shadow-sm" style="border-radius:14px">
            <div class="card-body text-center p-4">
                <div class="mx-auto mb-3 d-flex align-items-center justify-content-center"
                     style="width:72px;height:72px;border-radius:50%;background:#C8F135;color:#0D0F14;font-size:1.8rem;font-weight:800">
                    <sec:authentication property="principal.username" var="email"/>
                    <c:out value="${email.substring(0,1).toUpperCase()}"/>
                </div>
                <div class="fw-bold fs-5"><c:out value="${usuario.name}"/></div>
                <div class="text-muted" style="font-size:.85rem"><c:out value="${usuario.email}"/></div>
                <span class="badge mt-2" style="background:#EDE9DF;color:#0D0F14">
                    <c:out value="${usuario.rol}"/>
                </span>
            </div>
        </div>
    </div>

    <%-- ── Actualizar nombre ── --%>
    <div class="col-md-8">
        <div class="card border-0 shadow-sm mb-3" style="border-radius:14px">
            <div class="card-body p-4">
                <h6 class="fw-bold mb-3"><i class="bi bi-person me-2"></i>Actualizar información</h6>
                <form action="/perfil/${usuario.id}/actualizar" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="mb-3">
                        <label class="form-label" style="font-size:.82rem">Nombre completo</label>
                        <input type="text" name="name" class="form-control"
                               value="<c:out value='${usuario.name}'/>" required>
                    </div>
                    <button type="submit" class="btn fw-bold"
                            style="background:#0D0F14;color:#C8F135;border-radius:8px;padding:.55rem 1.4rem">
                        <i class="bi bi-check-lg me-1"></i>Guardar cambios
                    </button>
                </form>
            </div>
        </div>

        <%-- ── Cambiar contraseña ── --%>
        <div class="card border-0 shadow-sm" style="border-radius:14px">
            <div class="card-body p-4">
                <h6 class="fw-bold mb-3"><i class="bi bi-shield-lock me-2"></i>Cambiar contraseña</h6>
                <form action="/perfil/${usuario.id}/password" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="mb-3">
                        <label class="form-label" style="font-size:.82rem">Contraseña actual</label>
                        <input type="password" name="passwordActual" class="form-control" required>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Nueva contraseña</label>
                            <input type="password" name="passwordNueva" class="form-control"
                                   minlength="8" required>
                        </div>
                        <div class="col-6">
                            <label class="form-label" style="font-size:.82rem">Confirmar nueva</label>
                            <input type="password" name="confirmarPassword" class="form-control" required>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-outline-secondary fw-bold"
                            style="border-radius:8px;padding:.55rem 1.4rem">
                        <i class="bi bi-key me-1"></i>Actualizar contraseña
                    </button>
                </form>
            </div>
        </div>
    </div>

</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
