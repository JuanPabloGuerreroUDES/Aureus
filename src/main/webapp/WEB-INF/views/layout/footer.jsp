<%@ page contentType="text/html;charset=UTF-8" language="java" %>
</div><%-- cierre de .main-content --%>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    /**
     * Helper para incluir el token CSRF en peticiones fetch/AJAX.
     * Necesario porque Spring Security requiere el token en todas las
     * peticiones POST/PUT/DELETE (Unidad 9 - Sección 6.1).
     *
     * Uso: fetchWithCsrf('/api/recurso', { method:'POST', body: JSON.stringify(data) })
     */
    function getCsrfToken() {
        const meta = document.getElementById('csrf-meta');
        return {
            paramName: meta ? meta.dataset.param : '_csrf',
            token:     meta ? meta.content : ''
        };
    }

    async function fetchWithCsrf(url, options = {}) {
        const csrf = getCsrfToken();
        const headers = {
            'Content-Type': 'application/json',
            [csrf.paramName]: csrf.token,
            ...options.headers
        };
        return fetch(url, { ...options, headers });
    }

    // Auto-ocultar alertas después de 5 segundos
    document.querySelectorAll('.alert.alert-dismissible').forEach(alert => {
        setTimeout(() => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            bsAlert.close();
        }, 5000);
    });
</script>
</body>
</html>
