package com.aureus.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas E2E de la interfaz de usuario con Selenium WebDriver (U10 §5.4).
 *
 * Posición en la pirámide de pruebas (U10 §1.3):
 *   Cima — 10 % del total. Lentas y costosas; prueban el flujo real del usuario.
 *
 * @SpringBootTest(webEnvironment = DEFINED_PORT): levanta el servidor en el
 * puerto real (8080) para que Selenium pueda navegar contra él.
 *
 * Patrón Page Object Model (U10 §5.3): los tests no contienen lógica de
 * localización de elementos. LoginPage y DashboardPage encapsulan eso,
 * haciendo los tests legibles como especificaciones de comportamiento.
 *
 * Selenium Explicit Wait (U10 §5.5): las páginas cargan async;
 * los Page Objects usan WebDriverWait en lugar de Thread.sleep.
 *
 * NOTA: Estos tests requieren Chrome instalado y la aplicación corriendo.
 * En CI/CD, usar @Disabled o un perfil @Tag("e2e") para ejecutarlos
 * selectivamente con: mvn test -Dgroups=e2e
 *
 * Los tests usan las credenciales del DataInitializer (perfil 'dev'):
 *   demo@aureus.com / Demo2026!
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("dev")
@Tag("e2e")
@DisplayName("Login E2E — Selenium WebDriver")
class LoginE2ETest {

    private static final String BASE_URL  = "http://localhost:8080";
    private static final String EMAIL_OK  = "demo@aureus.com";
    private static final String PASS_OK   = "Demo2026!";
    private static final String PASS_FAIL = "contraseña_incorrecta";

    private WebDriver  driver;
    private LoginPage  loginPage;

    // ── Setup / Teardown (U10 §2.2 — @BeforeEach / @AfterEach) ──────────

    @BeforeEach
    void setUp() {
        // WebDriverManager descarga el chromedriver correcto automáticamente (U10 §5.1)
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // --headless: ejecuta sin ventana (necesario en servidores CI/CD)
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,800");

        driver    = new ChromeDriver(options);
        driver.get(BASE_URL + "/auth/login");

        // Page Object Model (U10 §5.3): crea la página, no busca elementos directamente
        loginPage = new LoginPage(driver);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();  // libera el proceso del navegador
        }
    }

    // ── Pruebas E2E ───────────────────────────────────────────────────────

    @Test
    @DisplayName("login exitoso redirige al dashboard y muestra el logout")
    void loginExitoso_redireccionaAlDashboardYMuestraLogout() {
        // When — Page Object encapsula la interacción
        DashboardPage dashboard = loginPage.loginExitoso(EMAIL_OK, PASS_OK);

        // Then — verifica que el usuario llegó al dashboard
        assertThat(dashboard.estaCargado()).isTrue();
        assertThat(dashboard.obtenerUrlActual()).contains("/dashboard");
        assertThat(dashboard.logoutEstaVisible()).isTrue();
    }

    @Test
    @DisplayName("login fallido con contraseña incorrecta permanece en login con mensaje de error")
    void loginFallido_permanecEnLoginConMensajeError() {
        // When — loginFallido permanece en la misma página (no hace redirect)
        LoginPage mismaPage = loginPage.loginFallido(EMAIL_OK, PASS_FAIL);

        // Then — debe mostrar un mensaje de error (no revelar si el email existe)
        String mensajeError = mismaPage.obtenerMensajeError();
        assertThat(mensajeError).isNotBlank();
        assertThat(mismaPage.obtenerUrlActual()).contains("/login");
    }

    @Test
    @DisplayName("logout invalida la sesión y redirige a login")
    void logout_invalidaSesionYRedireccionaALogin() {
        // Given — usuario autenticado
        DashboardPage dashboard = loginPage.loginExitoso(EMAIL_OK, PASS_OK);
        assertThat(dashboard.estaCargado()).isTrue();

        // When — hace logout
        LoginPage loginDespues = dashboard.hacerLogout();

        // Then — de vuelta en login, sesión cerrada
        assertThat(loginDespues.obtenerUrlActual()).contains("/login");
        assertThat(loginDespues.estaVisible()).isTrue();
    }
}
