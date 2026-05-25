<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aureus — Error <c:out value="${not empty codigo ? codigo : ''}"/></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        body { background:#0D0F14; min-height:100vh; display:flex;
               align-items:center; justify-content:center; flex-direction:column; }
        .error-code  { font-size:7rem; font-weight:800; color:#C8F135; line-height:1; }
        .error-title { font-size:1.5rem; font-weight:700; color:#fff; }
        .error-msg   { color:rgba(255,255,255,0.5); font-size:.95rem; max-width:420px; }
        .btn-home    { background:#C8F135; color:#0D0F14; border-radius:8px;
                       padding:.65rem 1.8rem; font-weight:700; font-size:.95rem; text-decoration:none; }
        .btn-home:hover { opacity:.9; color:#0D0F14; }
        .logo        { font-size:1.1rem; font-weight:800; color:#fff; margin-bottom:3rem; }
        .logo span   { color:#C8F135; }
    </style>
</head>
<body>
    <a class="logo" href="/dashboard" style="text-decoration:none">Au<span>reus</span></a>

    <div class="text-center px-3">
        <div class="error-code">
            <%-- FIX: mostrar código o icono por defecto si no viene del handler --%>
            <c:choose>
                <c:when test="${not empty codigo}"><c:out value="${codigo}"/></c:when>
                <c:otherwise><i class="bi bi-exclamation-triangle" style="font-size:5rem"></i></c:otherwise>
            </c:choose>
        </div>

        <div class="error-title mt-3">
            <c:out value="${not empty titulo ? titulo : 'Algo salió mal'}"/>
        </div>

        <div class="error-msg mt-2 mb-4 mx-auto">
            <c:out value="${not empty mensaje ? mensaje : 'Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde.'}"/>
        </div>

        <div class="d-flex gap-3 justify-content-center">
            <a href="/dashboard" class="btn-home">
                <i class="bi bi-house-fill me-2"></i>Ir al inicio
            </a>
            <button onclick="history.back()"
                    class="btn text-white" style="border:1px solid rgba(255,255,255,0.2);border-radius:8px;padding:.65rem 1.4rem">
                <i class="bi bi-arrow-left me-2"></i>Volver
            </button>
        </div>
    </div>
</body>
</html>
