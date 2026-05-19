<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aureus — Crear cuenta</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        :root { --ff-lime: #C8F135; --ff-ink: #0D0F14; --ff-teal: #1A9E8C; }
        body { background: var(--ff-ink); min-height:100vh;
               display:flex; align-items:center; justify-content:center; padding:2rem 0; }
        .reg-panel {
            background:#F5F2EC; border-radius:20px; overflow:hidden;
            max-width:880px; width:100%; box-shadow:0 40px 80px rgba(0,0,0,0.5);
        }
        .reg-left {
            background: linear-gradient(135deg,#0D0F14 60%,#1a1d28);
            padding:3rem; display:flex; flex-direction:column; justify-content:space-between;
        }
        .reg-logo  { font-size:1.4rem; font-weight:800; color:#fff; }
        .reg-logo span { color:var(--ff-lime); }
        .reg-headline { font-size:2rem; font-weight:800; color:#fff; line-height:1.15; }
        .reg-headline span { color:var(--ff-lime); }
        .feature-check { font-size:.83rem; color:rgba(255,255,255,.7); line-height:2; }
        .reg-right { padding:2.4rem; }
        .form-label  { font-size:.8rem; font-weight:600; color:#444; }
        .form-control {
            border-radius:8px; border:1.5px solid #D8D4CA;
            background:#fff; font-size:.9rem; padding:.6rem .85rem;
        }
        .form-control:focus { border-color:var(--ff-teal); box-shadow:0 0 0 3px rgba(26,158,140,0.15); }
        .form-control.error-field { border-color:#F25C3A; }
        .field-error { font-size:.75rem; color:#F25C3A; margin-top:.3rem; }
        .btn-register {
            background:var(--ff-ink); color:var(--ff-lime); font-weight:700;
            border:none; border-radius:8px; padding:.75rem; font-size:.95rem; width:100%;
        }
        .btn-register:hover { opacity:.9; color:var(--ff-lime); }
    </style>
</head>
<body>
<div class="reg-panel">
    <div class="row g-0">

        <div class="col-md-5 reg-left">
            <div class="reg-logo">Fin<span>Flow</span></div>
            <div>
                <div class="reg-headline">Tu viaje financiero <span>comienza aquí.</span></div>
                <p class="text-white-50 mt-2" style="font-size:.88rem">
                    Crea tu cuenta gratuita y empieza a gestionar tu dinero de forma inteligente.
                </p>
                <div class="mt-3 p-3 rounded" style="background:rgba(255,255,255,0.05)">
                    <div style="font-size:.75rem;color:rgba(255,255,255,.4);margin-bottom:.6rem">
                        ✓ Incluye en tu cuenta gratuita:
                    </div>
                    <div class="feature-check">
                        ✦ Registro ilimitado de ingresos y gastos<br>
                        ✦ Hasta 5 metas de ahorro<br>
                        ✦ Reportes mensuales básicos<br>
                        ✦ Motor de simulación financiera
                    </div>
                </div>
            </div>
            <div style="font-size:.7rem;color:rgba(255,255,255,.2)">
                Las contraseñas se almacenan cifradas con BCrypt.
            </div>
        </div>

        <div class="col-md-7 reg-right">
            <h4 class="fw-bold mb-1" style="color:#0D0F14">Crear cuenta</h4>
            <p class="text-muted mb-4" style="font-size:.85rem">Completa tus datos para comenzar</p>

            <c:if test="${not empty errorMsg}">
                <div class="alert alert-danger d-flex align-items-center gap-2 py-2 mb-3" style="font-size:.85rem">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    <c:out value="${errorMsg}"/>
                </div>
            </c:if>

            <%--
                Spring MVC form tag genera el HTML con binding a RegistroDto.
                Incluye automáticamente el token CSRF cuando se usa <form:form>.
            --%>
            <form:form action="/auth/registro" method="post" modelAttribute="registroDto">

                <div class="row g-2 mb-3">
                    <div class="col-12">
                        <label class="form-label">Nombre completo</label>
                        <form:input path="name" cssClass="form-control" placeholder="Juan García"/>
                        <form:errors path="name" cssClass="field-error"/>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Correo electrónico</label>
                    <form:input path="email" type="email" cssClass="form-control"
                                placeholder="tucorreo@email.com"/>
                    <form:errors path="email" cssClass="field-error"/>
                </div>

                <div class="row g-2 mb-3">
                    <div class="col-6">
                        <label class="form-label">Contraseña</label>
                        <form:password path="password" cssClass="form-control" placeholder="••••••••"/>
                        <form:errors path="password" cssClass="field-error"/>
                        <div style="font-size:.7rem;color:#6E7080;margin-top:.3rem">Mínimo 8 caracteres</div>
                    </div>
                    <div class="col-6">
                        <label class="form-label">Confirmar contraseña</label>
                        <form:password path="confirmPassword" cssClass="form-control" placeholder="••••••••"/>
                        <form:errors path="confirmPassword" cssClass="field-error"/>
                    </div>
                </div>

                <button type="submit" class="btn btn-register">Crear mi cuenta →</button>
            </form:form>

            <div class="text-center mt-3" style="font-size:.82rem;color:#6E7080">
                ¿Ya tienes cuenta?
                <a href="/auth/login" style="color:#1A9E8C;font-weight:600;text-decoration:none">
                    Inicia sesión
                </a>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
