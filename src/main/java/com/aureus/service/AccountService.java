package com.aureus.service;

import com.aureus.dto.account.CuentaDto;
import com.aureus.model.Account;
import com.aureus.model.User;

import java.util.List;

public interface AccountService {

    /** Lista todas las cuentas del usuario: la principal primero, luego las demás por nombre. */
    List<Account> listar(User usuario);

    /** Devuelve la cuenta principal del usuario (siempre existe al menos una). */
    Account obtenerPrincipal(User usuario);

    /** Crea una nueva cuenta opcional para el usuario. */
    Account crear(CuentaDto dto, User usuario);

    /** Edita nombre, icono y descripción de una cuenta. */
    Account editar(Long id, CuentaDto dto, User usuario);

    /**
     * Marca la cuenta indicada como principal y quita el flag de la anterior.
     * No puede haber más de una cuenta principal por usuario.
     */
    void marcarComoPrincipal(Long id, User usuario);

    /**
     * Elimina una cuenta.
     * Regla: la cuenta principal NUNCA puede eliminarse directamente.
     * El usuario debe primero transferir el rol a otra cuenta.
     */
    void eliminar(Long id, User usuario);
}
