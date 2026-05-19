package com.aureus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando un recurso no existe O el usuario no tiene acceso.
 * El mensaje genérico previene revelar si el recurso existe (IDOR - A01 OWASP).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontradoException extends AureusException {
    public RecursoNoEncontradoException(String entidad, Long id) {
        super(entidad + " con id " + id + " no encontrada");
    }
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
