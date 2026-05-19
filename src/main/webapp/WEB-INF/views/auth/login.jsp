<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aureus — Iniciar sesión</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        :root { --au-lime: #C8F135; --au-ink: #0D0F14; --au-teal: #1A9E8C; }
        body { background: var(--au-ink); min-height: 100vh;
               display: flex; align-items: center; justify-content: center; }
        .login-panel {
            background: #F5F2EC; border-radius: 20px;
            overflow: hidden; max-width: 880px; width: 100%;
            box-shadow: 0 40px 80px rgba(0,0,0,0.5);
        }
        .login-left {
            background: linear-gradient(135deg, #0D0F14 60%, #1a1d28);
            padding: 3rem;
            display: flex; flex-direction: column; justify-content: space-between;
            min-height: 480px;
        }
        .login-logo { font-size: 1.4rem; font-weight: 800; color: #fff; }
        .login-logo span { color: var(--au-lime); }
        .login-headline { font-size: 2.2rem; font-weight: 800; color: #fff; line-height: 1.15; }
        .login-headline span { color: var(--au-lime); }
        .login-stat-val { font-size: 1.4rem; font-weight: 800; color: var(--au-lime); }
        .login-stat-label { font-size: 0.7rem; color: rgba(255,255,255,0.4); }
        .login-right { padding: 2.8rem; background: #F5F2EC; }
        .form-label { font-size: 0.8rem; font-weight: 600; color: #444; }
        .form-control { border-radius: 8px; border: 1.5px solid #D8D4CA;
                        background: #fff; font-size: 0.9rem; padding: .6rem .85rem; }
        .form-control:focus { border-color: var(--au-teal); box-shadow: 0 0 0 3px rgba(26,158,140,0.15); }
        .btn-login {
            background: var(--au-ink); color: var(--au-lime);
            font-weight: 700; border: none; border-radius: 8px;
            padding: .75rem; font-size: 0.95rem; width: 100%; margin-top: .5rem;
        }
        .btn-login:hover { opacity: .9; color: var(--au-lime); }
    </style>
</head>
<body>
<div class="login-panel">
    <div class="row g-0">

        <%-- Panel izquierdo decorativo --%>
        <div class="col-md-5 login-left">
            <div class="login-logo">Au<span>reus</span></div>
            <div>
                <div class="login-headline">
                    Controla tu dinero.<br>Construye tu <span>futuro.</span>
                </div>
                <p class="text-white-50 mt-2" style="font-size:.88rem">
                    Simula escenarios, establece metas y toma decisiones financieras inteligentes.
                </p>
            </div>
            <div class="d-flex gap-4">
                <div>
                    <div class="login-stat-val">+4.2k</div>
                    <div class="login-stat-label">Usuarios activos</div>
                </div>
                <div>
                    <div class="login-stat-val">97%</div>
                    <div class="login-stat-label">Satisfacción</div>
                </div>
            </div>
        </div>

        <%-- Panel derecho: formulario de login --%>
        <div class="col-md-7 login-right">
            <h4 class="fw-bold mb-1" style="color:#0D0F14">Bienvenido de vuelta</h4>
            <p class="text-muted mb-4" style="font-size:.85rem">Ingresa a tu cuenta para continuar</p>

            <%-- Mensajes de error / logout --%>
            <c:if test="${not empty errorMsg}">
                <div class="alert alert-danger d-flex align-items-center gap-2 py-2" style="font-size:.85rem">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    <c:out value="${errorMsg}"/>
                </div>
            </c:if>
            <c:if test="${not empty logoutMsg}">
                <div class="alert alert-success d-flex align-items-center gap-2 py-2" style="font-size:.85rem">
                    <i class="bi bi-check-circle-fill"></i>
                    <c:out value="${logoutMsg}"/>
                </div>
            </c:if>
            <c:if test="${not empty successMsg}">
                <div class="alert alert-success d-flex align-items-center gap-2 py-2" style="font-size:.85rem">
                    <i class="bi bi-check-circle-fill"></i>
                    <c:out value="${successMsg}"/>
                </div>
            </c:if>

            <%--
                Formulario de login.
                Spring Security intercepta el POST en /auth/login.
                Seguridad:
                  - Token CSRF incluido (protección CSRF - Unidad 9, Sección 6.1)
                  - th:action equivalente en JSP: action con context path
                  - Método POST (nunca GET — las credenciales no deben ir en la URL)
            --%>
            <form action="${pageContext.request.contextPath}/auth/do-login" method="post">
                <%-- Token CSRF — OBLIGATORIO en todos los formularios POST --%>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                <div class="mb-3">
                    <label for="email" class="form-label">Correo electrónico</label>
                    <input type="email" class="form-control" id="email"
                           name="email" placeholder="tucorreo@email.com"
                           autocomplete="email" required>
                </div>

                <div class="mb-1">
                    <label for="password" class="form-label">Contraseña</label>
                    <input type="password" class="form-control" id="password"
                           name="password" placeholder="••••••••"
                           autocomplete="current-password" required>
                </div>

                <div class="text-end mb-3">
                    <a href="#" style="font-size:.8rem;color:#1A9E8C;text-decoration:none">
                        ¿Olvidaste tu contraseña?
                    </a>
                </div>

                <button type="submit" class="btn btn-login">
                    Iniciar sesión →
                </button>
            </form>

            <div class="text-center mt-3" style="font-size:.82rem;color:#6E7080">
                ¿No tienes cuenta?
                <a href="${pageContext.request.contextPath}/auth/registro"
                   style="color:#1A9E8C;font-weight:600;text-decoration:none">
                    Regístrate gratis
                </a>
            </div>

            <%-- Credenciales de demo (solo en dev) --%>
            <div class="mt-4 p-3 rounded" style="background:#EDE9DF;font-size:.75rem;color:#6E7080">
                <strong>Demo:</strong> demo@aureus.com / Demo2026! &nbsp;|&nbsp;
                <strong>Admin:</strong> admin@aureus.com / Admin2026!
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
