package com.aureus.exception;

/**
 * Excepción base del dominio Aureus.
 * Todas las excepciones de negocio extienden esta clase (U11 §5.4).
 * Permite capturarlas en bloque en el GlobalExceptionHandler si es necesario.
 */
public abstract class AureusException extends RuntimeException {
    protected AureusException(String mensaje) {
        super(mensaje);
    }
}
