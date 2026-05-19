package com.aureus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailDuplicadoException extends AureusException {
    public EmailDuplicadoException(String email) {
        super("Ya existe una cuenta registrada con el correo: " + email);
    }
}
