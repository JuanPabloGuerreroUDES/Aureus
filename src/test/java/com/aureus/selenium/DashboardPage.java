package com.aureus.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object para el Dashboard de Aureus (U10 §5.3).
 *
 * Encapsula los elementos y acciones disponibles en el dashboard
 * para los tests E2E. Si el HTML del dashboard cambia (renombrar IDs,
 * mover elementos), solo este archivo necesita actualizarse.
 *
 * Implementa el patrón de retorno de Page Objects:
 *   loginPage.loginExitoso(...) → DashboardPage
 *   dashboardPage.irATransacciones() → TransactionPage (futuro)
 */
public class DashboardPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // ── Selectores ────────────────────────────────────────────────────────

    /** Título o encabezado del dashboard */
    private final By tituloPrincipal = By.cssSelector("h1, h2, .dashboard-title");

    /** Enlace de logout en la barra de navegación */
    private final By enlaceLogout    = By.cssSelector("a[href*='logout'], form[action*='logout'] button");

    /** Card de balance del mes */
    private final By cardBalance     = By.id("balance-neto");

    /** Nombre del usuario autenticado en el header */
    private final By nombreUsuario   = By.cssSelector(".navbar-text, .user-name, #usuario-nombre");

    // ── Constructor ───────────────────────────────────────────────────────

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ── Consultas del estado ──────────────────────────────────────────────

    /**
     * Retorna el texto del título principal del dashboard.
     * Usado en assertions: assertThat(dashboard.getTitulo()).contains("Dashboard")
     */
    public String getTitulo() {
        WebElement titulo = wait.until(
                ExpectedConditions.visibilityOfElementLocated(tituloPrincipal));
        return titulo.getText();
    }

    /**
     * Verifica si el dashboard está completamente cargado
     * comprobando que la URL contiene "/dashboard".
     */
    public boolean estaCargado() {
        return driver.getCurrentUrl().contains("/dashboard");
    }

    /**
     * Retorna la URL actual del dashboard.
     */
    public String obtenerUrlActual() {
        return driver.getCurrentUrl();
    }

    /**
     * Verifica si el enlace/botón de logout está visible en la página.
     * Confirma que el usuario está efectivamente autenticado.
     */
    public boolean logoutEstaVisible() {
        try {
            return driver.findElement(enlaceLogout).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    // ── Acciones ──────────────────────────────────────────────────────────

    /**
     * Hace logout y retorna la LoginPage resultante.
     */
    public LoginPage hacerLogout() {
        WebElement btnLogout = wait.until(
                ExpectedConditions.elementToBeClickable(enlaceLogout));
        btnLogout.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        return new LoginPage(driver);
    }
}
