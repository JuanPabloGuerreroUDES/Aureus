<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aureus — <c:out value="${pageTitle != null ? pageTitle : 'Simulador de Presupuesto'}"/></title>

    <%-- Bootstrap 5 --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <%-- Bootstrap Icons --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"
          rel="stylesheet">

    <style>
        :root {
            --au-lime:   #C8F135;
            --au-ink:    #0D0F14;
            --au-teal:   #1A9E8C;
            --au-coral:  #F25C3A;
            --au-gold:   #E8A912;
            --au-muted:  #6E7080;
            --sidebar-w: 240px;
        }
        body { background: #F5F2EC; font-family: 'Segoe UI', sans-serif; }

        /* ── Sidebar ── */
        .sidebar {
            width: var(--sidebar-w);
            min-height: 100vh;
            background: var(--au-ink);
            position: fixed; top:0; left:0; z-index: 100;
            display: flex; flex-direction: column;
        }
        .sidebar .logo {
            font-size: 1.3rem; font-weight: 800;
            padding: 1.2rem 1.4rem; border-bottom: 1px solid rgba(255,255,255,0.08);
            color: #fff; text-decoration: none;
        }
        .sidebar .logo span { color: var(--au-lime); }
        .sidebar .nav-section {
            font-size: 0.65rem; font-weight: 700;
            letter-spacing: .1em; text-transform: uppercase;
            color: rgba(255,255,255,0.25); padding: .9rem 1.2rem .3rem;
        }
        .sidebar .nav-link {
            color: rgba(255,255,255,0.5); font-size: 0.875rem;
            padding: .55rem 1.2rem; border-left: 3px solid transparent;
            border-radius: 0 8px 8px 0; margin: 1px 8px 1px 0;
            display: flex; align-items: center; gap: .6rem;
        }
        .sidebar .nav-link:hover { color: #fff; background: rgba(255,255,255,0.05); }
        .sidebar .nav-link.active {
            color: var(--au-lime); border-left-color: var(--au-lime);
            background: rgba(200,241,53,0.08);
        }
        .sidebar .user-card {
            margin-top: auto; padding: 1rem 1.2rem;
            border-top: 1px solid rgba(255,255,255,0.08);
            color: rgba(255,255,255,0.7); font-size: 0.8rem;
        }
        .sidebar .user-avatar {
            width: 32px; height: 32px; border-radius: 50%;
            background: var(--au-lime); color: var(--au-ink);
            font-weight: 800; display: flex; align-items: center; justify-content: center;
            font-size: 0.85rem; flex-shrink: 0;
        }

        /* ── Main content ── */
        .main-content { margin-left: var(--sidebar-w); padding: 1.8rem 2rem; }

        /* ── KPI cards ── */
        .kpi-card { background:#fff; border-radius:12px; padding:1.2rem; border:1px solid #E8E5DF; }
        .kpi-card.lime  { background: var(--au-lime); border-color: #9DC41A; }
        .kpi-card.teal  { background: var(--au-teal); color:#fff; border-color: var(--au-teal); }
        .kpi-value { font-size: 1.6rem; font-weight: 700; line-height:1; }
        .kpi-label { font-size: 0.7rem; font-weight:600; text-transform:uppercase;
                     letter-spacing:.06em; opacity:.6; margin-bottom:.4rem; }

        /* ── Progress bars ── */
        .progress-ok   .progress-bar { background: var(--au-teal); }
        .progress-warn .progress-bar { background: var(--au-gold); }
        .progress-over .progress-bar { background: var(--au-coral); }

        /* ── Alerts ── */
        .alert-aureus {
            border-radius: 10px; font-size: 0.85rem;
            display: flex; align-items: center; gap: .6rem;
        }

        /* ── Badges ── */
        .badge-income  { background: #E0F5F2; color: var(--au-teal); }
        .badge-expense { background: #FEE9E4; color: var(--au-coral); }
        .badge-fixed   { background: #EDE9DF; color: var(--au-muted); }

        /* ── Tables ── */
        .table-aureus th {
            font-size: 0.7rem; font-weight: 700;
            letter-spacing: .06em; text-transform: uppercase;
            color: var(--au-muted); background: #F5F2EC;
        }
        .table-aureus td { vertical-align: middle; font-size: 0.875rem; }

        /* ── Botón cerrar sesión ── */
        .btn-logout {
            background: rgba(255,255,255,0.07);
            color: rgba(255,255,255,0.75);
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: 600;
            padding: .45rem .8rem;
            text-align: left;
            transition: background .18s, color .18s, border-color .18s;
        }
        .btn-logout:hover {
            background: var(--au-coral);
            color: #fff;
            border-color: var(--au-coral);
        }

        /* ── CSRF token hidden (accesible en JS si se necesita) ── */
        #csrf-meta { display: none; }
    </style>
</head>
<body>

<%-- ── CSRF meta tag para uso en fetch/AJAX si se necesita ── --%>
<meta id="csrf-meta"
      name="_csrf" content="${_csrf.token}"
      data-param="${_csrf.parameterName}">

<%-- ── SIDEBAR ── --%>
<aside class="sidebar">
    <a class="logo" href="/dashboard">Au<span>reus</span></a>

    <div class="nav-section">Principal</div>
    <a class="nav-link ${pageId == 'dashboard' ? 'active' : ''}" href="/dashboard">
        <i class="bi bi-grid-1x2"></i> Dashboard
    </a>
    <a class="nav-link ${pageId == 'transacciones' ? 'active' : ''}" href="/transacciones">
        <i class="bi bi-arrow-down-up"></i> Transacciones
    </a>
    <a class="nav-link ${pageId == 'presupuestos' ? 'active' : ''}" href="/presupuestos">
        <i class="bi bi-bullseye"></i> Presupuesto
    </a>
    <a class="nav-link ${pageId == 'metas' ? 'active' : ''}" href="/metas">
        <i class="bi bi-trophy"></i> Metas de ahorro
    </a>

    <div class="nav-section">Análisis</div>
    <a class="nav-link ${pageId == 'reportes' ? 'active' : ''}" href="/reportes">
        <i class="bi bi-bar-chart-line"></i> Reportes
    </a>

    <sec:authorize access="hasRole('ADMIN')">
    <div class="nav-section">Admin</div>
    <a class="nav-link ${pageId == 'admin' ? 'active' : ''}" href="/admin">
        <i class="bi bi-shield-lock"></i> Panel Admin
    </a>
    </sec:authorize>

    <div class="user-card">
        <%-- Info del usuario con link a perfil --%>
        <a href="/perfil" class="d-flex align-items-center gap-2 mb-2 text-decoration-none"
           style="color:inherit">
            <div class="user-avatar">
                <sec:authentication property="principal.username" var="userEmail"/>
                <c:out value="${userEmail.substring(0,1).toUpperCase()}"/>
            </div>
            <div class="flex-grow-1 text-truncate">
                <div class="text-white fw-500" style="font-size:.8rem">
                    <sec:authentication property="principal.username"/>
                </div>
                <div style="font-size:.7rem;opacity:.4;color:#C8F135">Ver perfil →</div>
            </div>
        </a>
        <%-- Botón de cierre de sesión destacado --%>
        <form action="/auth/logout" method="post" class="m-0">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit" class="btn btn-logout w-100">
                <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión
            </button>
        </form>
    </div>
</aside>

<%-- ── MAIN CONTENT ── --%>
<div class="main-content">
    <%-- Alertas flash --%>
    <c:if test="${not empty successMsg}">
        <div class="alert alert-success alert-aureus alert-dismissible fade show mb-3">
            <i class="bi bi-check-circle-fill"></i> <c:out value="${successMsg}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger alert-aureus alert-dismissible fade show mb-3">
            <i class="bi bi-exclamation-triangle-fill"></i> <c:out value="${errorMsg}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
