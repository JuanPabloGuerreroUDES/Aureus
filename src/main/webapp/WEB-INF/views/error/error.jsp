<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aureus — Error ${codigo}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background:#0D0F14; min-height:100vh; display:flex;
               align-items:center; justify-content:center; }
        .error-code { font-size:6rem; font-weight:800; color:#C8F135; line-height:1; }
        .error-title { font-size:1.4rem; font-weight:700; color:#fff; }
        .error-msg { color:rgba(255,255,255,0.5); font-size:.9rem; }
    </style>
</head>
<body>
<div class="text-center px-3">
    <div class="error-code"><c:out value="${codigo}"/></div>
    <div class="error-title mt-2"><c:out value="${titulo}"/></div>
    <div class="error-msg mt-2 mb-4"><c:out value="${mensaje}"/></div>
    <a href="/dashboard" class="btn fw-bold"
       style="background:#C8F135;color:#0D0F14;border-radius:8px;padding:.6rem 1.5rem">
        ← Volver al inicio
    </a>
</div>
</body>
</html>
