package com.aureus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Previene IDOR (A01 OWASP) — recurso existe pero no pertenece al usuario. */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccesoDenegadoException extends AureusException {
    public AccesoDenegadoException() {
        super("No tienes permiso para acceder a este recurso");
    }
}
