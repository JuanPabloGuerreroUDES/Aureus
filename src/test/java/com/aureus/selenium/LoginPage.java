package com.aureus.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object para la página de login de Aureus (U10 §5.3).
 *
 * El patrón Page Object Model (POM) encapsula la lógica de interacción
 * con la página en una clase separada, separando:
 *   - La lógica de navegación/interacción (aquí, en los métodos).
 *   - La lógica de aserción (en la clase de prueba E2E).
 *
 * Esto reduce el impacto de cambios en la UI: si cambia el ID de un campo,
 * solo se modifica este archivo, no todos los tests que usan login.
 *
 * Selectores: se usa By.id (prioridad 1) y By.cssSelector (prioridad 3)
 * según la tabla de selectores recomendados (U10 §5.2).
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Selectores (U10 §5.2 — orden de prioridad) ───────────────────────

    /** By.id: prioridad 1 — el selector más estable */
    private final By campoEmail    = By.id("email");
    private final By campoPassword = By.id("password");

    /** By.cssSelector: prioridad 3 — más legible que XPath */
    private final By btnSubmit     = By.cssSelector("button[type='submit']");

    /** Alertas de error — clase CSS del componente Bootstrap */
    private final By alertaDanger  = By.cssSelector(".alert-danger");
    private final By alertaSuccess = By.cssSelector(".alert-success");

    // ── Constructor ───────────────────────────────────────────────────────

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        // Explicit Wait: espera hasta 10s por condición específica (U10 §5.5 recomendado)
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ── Acciones (devuelven Page Objects para encadenado fluido) ──────────

    /**
     * Intenta autenticarse con las credenciales proporcionadas.
     * Si el login es exitoso, retorna la DashboardPage resultante.
     *
     * @param email    correo del usuario
     * @param password contraseña
     * @return DashboardPage si la autenticación fue exitosa
     */
    public DashboardPage loginExitoso(String email, String password) {
        completarFormulario(email, password);
        // Explicit Wait: espera a que el dashboard cargue tras el redirect (U10 §5.5)
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        return new DashboardPage(driver);
    }

    /**
     * Intenta login con credenciales inválidas y permanece en la página de login.
     *
     * @param email    correo (posiblemente incorrecto)
     * @param password contraseña incorrecta
     * @return this — permanece en LoginPage para verificar el mensaje de error
     */
    public LoginPage loginFallido(String email, String password) {
        completarFormulario(email, password);
        // Espera a que aparezca la alerta de error
        wait.until(ExpectedConditions.visibilityOfElementLocated(alertaDanger));
        return this;
    }

    // ── Consultas del estado de la página ────────────────────────────────

    /** Retorna el texto del mensaje de error si existe. */
    public String obtenerMensajeError() {
        WebElement alerta = wait.until(
                ExpectedConditions.visibilityOfElementLocated(alertaDanger));
        return alerta.getText();
    }

    /** Verifica que el campo email esté visible (la página cargó correctamente). */
    public boolean estaVisible() {
        return driver.findElement(campoEmail).isDisplayed();
    }

    /** Retorna la URL actual (útil para verificar redirecciones). */
    public String obtenerUrlActual() {
        return driver.getCurrentUrl();
    }

    // ── Helper privado ────────────────────────────────────────────────────

    private void completarFormulario(String email, String password) {
        driver.findElement(campoEmail).clear();
        driver.findElement(campoEmail).sendKeys(email);
        driver.findElement(campoPassword).clear();
        driver.findElement(campoPassword).sendKeys(password);
        driver.findElement(btnSubmit).click();
    }
}
